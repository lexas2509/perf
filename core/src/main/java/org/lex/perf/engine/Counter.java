package org.lex.perf.engine;

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
public class Counter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Counter.class);

    private int slotDuration = 1000; // in ms;

    private static final int NUM_OF_SLOT = 10;

    private AtomicReferenceArray<CounterTimeSlot> timeSlots = new AtomicReferenceArray<CounterTimeSlot>(NUM_OF_SLOT);

    private RrdDb rrdDb;

    public Counter() {
        try {
            RrdDef rrdDef = new RrdDef("e:/test.rrd");
            rrdDef.setStartTime(Util.getTime() - 1);
            rrdDef.addDatasource("hits", DsType.ABSOLUTE, 1, 0, Double.NaN);
            rrdDef.addDatasource("value", DsType.ABSOLUTE, 1, 0, Double.NaN);
            rrdDef.setStep(10);
            rrdDef.addArchive(ConsolFun.TOTAL, 0.5, 1, 7 * 24 * 60);
            rrdDb = new RrdDb(rrdDef);
            rrdDb.close();
            rrdDb = new RrdDb(rrdDef);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CounterTimeSlot getTimeSlot(long eventTime) {
        // timeslots are rings of slots
        long l = (eventTime / slotDuration);
        int n = (int) l % NUM_OF_SLOT;

        CounterTimeSlot timeSlot = timeSlots.get(n);

        // if slot if from the past;
        if ((timeSlot != null) && (eventTime >= timeSlot.getEndTime())) {
            timeSlot = null;
            // Start cleaning
            doCleanup(eventTime);
        }

        // if timeSlot not initialized  - do initialize
        if (timeSlot == null) {
            long startTime = l * slotDuration;
            timeSlot = new CounterTimeSlot(startTime, startTime + slotDuration);
            if (!timeSlots.compareAndSet(n, null, timeSlot)) {
                timeSlot = timeSlots.get(n);
            }
        }

        if (eventTime < timeSlot.getStartTime()) {
            return null;
        }

        return timeSlot;
    }

    public void doCleanup(long eventTime) {
        long l = (eventTime / slotDuration);
        int n = (int) l % NUM_OF_SLOT;

        List<CounterTimeSlot> slotsToFlush = new ArrayList<CounterTimeSlot>();
        for (int i = n; i < NUM_OF_SLOT + n; i++) {
            int idx = i % NUM_OF_SLOT;
            CounterTimeSlot slot = timeSlots.get(idx);
            if ((slot != null) && (eventTime > slot.getEndTime())) {
                if (timeSlots.compareAndSet(idx, slot, null)) {
                    slotsToFlush.add(slot);
                }
            }
        }

        Collections.sort(slotsToFlush, new Comparator<CounterTimeSlot>() {
            @Override
            public int compare(CounterTimeSlot o1, CounterTimeSlot o2) {
                if (o1.getStartTime() < o2.getStartTime()) return -1;
                if (o1.getStartTime() > o2.getStartTime()) return 1;
                return 0;
            }
        });

        for (CounterTimeSlot cts : slotsToFlush) {
            flush(cts);
        }
    }

    private void flush(CounterTimeSlot timeSlot) {
        Sample sample = null;
        try {
            sample = rrdDb.createSample();
            sample.setTime(timeSlot.getEndTime() / 1000);
            sample.setValue("hits", timeSlot.getCount());
            sample.setValue("value", timeSlot.getTotal());
            sample.update();
            LOGGER.warn(Long.toString(timeSlot.getEndTime()));
            rrdDb.dumpXml("e:/ddd.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}