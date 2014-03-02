package org.lex.perf.engine;

import org.lex.perf.event.MonitoringCategory;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 */
public abstract class Index<T extends TimeSlot> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Index.class);

    protected int slotDuration = 1000; // in ms;

    private static final int NUM_OF_SLOT = 10;

    private AtomicReferenceArray<T> timeSlots = new AtomicReferenceArray<T>(NUM_OF_SLOT);

    protected RrdDb rrdDb;

    protected final String counterName;

    public Index(MonitoringCategory category, String name) {
        counterName = category.getName() + "-" + name;
    }

    public T getTimeSlot(long eventTime) {
        // timeslots are ringbuffer
        long l = (eventTime / slotDuration);
        int n = (int) l % NUM_OF_SLOT;

        T timeSlot = timeSlots.get(n);

        // if slot if from the past;
        if ((timeSlot != null) && (eventTime >= timeSlot.getEndTime())) {
            timeSlot = null;
            // Start cleaning
            doCleanup(eventTime);
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
            return null;
        }

        return timeSlot;
    }

    protected abstract T createTimeSlot(long startTime);

    public void doCleanup(long eventTime) {
        long l = (eventTime / slotDuration);
        int n = (int) l % NUM_OF_SLOT;

        List<T> slotsToFlush = new ArrayList<T>();
        for (int i = n; i < NUM_OF_SLOT + n; i++) {
            int idx = i % NUM_OF_SLOT;
            T slot = timeSlots.get(idx);
            if ((slot != null) && (eventTime > slot.getEndTime())) {
                if (timeSlots.compareAndSet(idx, slot, null)) {
                    slotsToFlush.add(slot);
                }
            }
        }

        Collections.sort(slotsToFlush, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                if (o1.getStartTime() < o2.getStartTime()) return -1;
                if (o1.getStartTime() > o2.getStartTime()) return 1;
                return 0;
            }
        });

        for (T cts : slotsToFlush) {
            flush(cts);
        }

    }

    private void flush(T timeSlot) {
        Sample sample = null;
        try {
            sample = rrdDb.createSample();
            sample.setTime(timeSlot.getEndTime() / 1000);
            timeSlot.storeData(sample);
            sample.update();
            rrdDb.dumpXml("e:/counter-" + ".xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public abstract String getFileName();
}