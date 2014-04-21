package org.lex.perf.engine;

import org.lex.perf.event.MonitoringCategory;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 */
public abstract class Index<T extends TimeSlot> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Index.class);

    protected int slotDuration = Engine.SAMPLE_DURATION; // in ms;

    private static final int NUM_OF_SLOT = 6;

    private AtomicReferenceArray<T> timeSlots = new AtomicReferenceArray<T>(NUM_OF_SLOT);

    protected RrdDb rrdDb;

    protected final MonitoringCategory category;

    protected final String itemName;

    protected final String fileName;

    protected Queue<T> slotsToFlush = new ConcurrentLinkedQueue<T>();

    protected AtomicLong sampleTime = new AtomicLong();

    public Index(MonitoringCategory category, String name) {
        this.category = category;
        this.itemName = name;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bn = md.digest(name.getBytes("UTF-8"));
            String n = DatatypeConverter.printHexBinary(bn);
            fileName = category.getName() + n;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        sampleTime.set((System.currentTimeMillis() / slotDuration) * slotDuration);
    }

    public T getTimeSlot(long eventTime) {
        // timeslots are ringbuffer
        long l = (eventTime / slotDuration);
        int n = (int) l % NUM_OF_SLOT;

        T timeSlot = timeSlots.get(n);

        // if slot is from the past;
        if ((timeSlot != null) && (eventTime >= timeSlot.getEndTime())) {
            if (timeSlots.compareAndSet(n, timeSlot, null)) {
                slotsToFlush.add(timeSlot);
            }
            timeSlot = null;
        }

        // if timeSlot not initialized  - do initialize
        if (timeSlot == null) {
            long startTime = l * slotDuration;
            timeSlot = createTimeSlot(startTime);
            if (!timeSlots.compareAndSet(n, null, timeSlot)) {
                timeSlot = timeSlots.get(n);
            }
        }

        if (eventTime < timeSlot.getStartTime()) {
            LOGGER.error("Skip event {} for index {}", eventTime, itemName);
            return null;
        }

        return timeSlot;
    }

    protected abstract T createTimeSlot(long startTime);

    private void flush(T timeSlot) {
        Sample sample = null;
        try {
            long currentTime = timeSlot.getEndTime() / 1000;
            sample = rrdDb.createSample();
            sample.setTime(currentTime);
//            LOGGER.warn(itemName + ":" + Long.toString(sample.getTime()));
            timeSlot.storeData(sample);
            if (rrdDb.getLastUpdateTime() >= currentTime) {
                new Object();
            }
            sample.update();
//            rrdDb.dumpXml("e:/mondata/counter-" + fileName + ".xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String getFileName() {
        return fileName;
    }

    public String getIndexName() {
        return itemName;
    }

    public void doSample(long currentTime) {
        long newSampleTime = (currentTime / slotDuration) * slotDuration;

        TreeMap<Long, T> slotsToFlush = new TreeMap<Long, T>();
        while (!this.slotsToFlush.isEmpty()) {
            T t = this.slotsToFlush.peek();
            if (t.getEndTime() < newSampleTime) {
                this.slotsToFlush.remove();
                slotsToFlush.put(t.getStartTime(), t);
            } else {
                break;
            }
        }


        long oldSampleTime = sampleTime.get();
        for (long time = oldSampleTime; time < newSampleTime; time = time + slotDuration) {
            long l = (time / slotDuration);
            int n = (int) l % NUM_OF_SLOT;
            T slot = timeSlots.get(n);
            if (slot == null) {
                slot = createTimeSlot(time);
                LOGGER.warn("dummy timeslot created {} ", slot.getStartTime()/1000);
            } else {
                if (!timeSlots.compareAndSet(n, slot, null)) {
                    new Object();
                }
            }
            slotsToFlush.put(slot.getStartTime(), slot);
        }
        sampleTime.compareAndSet(oldSampleTime, newSampleTime);

        for (Map.Entry<Long, T> cts : slotsToFlush.entrySet()) {
            flush(cts.getValue());
        }
    }

    public int getSlotDuration() {
        return slotDuration;
    }
}