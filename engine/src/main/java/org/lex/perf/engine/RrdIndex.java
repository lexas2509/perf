package org.lex.perf.engine;

import org.rrd4j.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 */
public abstract class RrdIndex<T extends TimeSlot> implements EngineIndex {
    private static final Logger LOGGER = LoggerFactory.getLogger(RrdIndex.class);

    protected int slotDuration; // in ms;

    private static final int NUM_OF_SLOT = 6;

    private AtomicReferenceArray<T> timeSlots = new AtomicReferenceArray<T>(NUM_OF_SLOT);

    protected final EngineImpl engine;

    protected RrdDb rrdDb;

    protected final String indexName;

    protected final String fileName;

    protected Queue<T> slotsToFlush = new ConcurrentLinkedQueue<T>();

    protected AtomicLong sampleTime = new AtomicLong();

    public RrdIndex(EngineImpl engine, String name, String fileName) {
        this.engine = engine;
        this.indexName = name;
        this.fileName = engine.getWorkingDirectory() + "/" + fileName + ".rrd";
        slotDuration = engine.getSlotDuration();
        sampleTime.set((System.currentTimeMillis() / slotDuration) * slotDuration);
    }

    public final void init() {
        try {
            RrdDef rrdDef = getRrdDef();
            if (new File(getFileName()).exists()) {
                try {
                    rrdDb = new RrdDb(getFileName(), false);
                    if (!checkRrdDef(rrdDef, rrdDb.getRrdDef())) {
                        rrdDb = null;
                    }
                } catch (IOException e) {
                    rrdDb = null;
                } catch (RuntimeException e) {
                    rrdDb = null;
                }
            }
            if (rrdDb == null) {
                rrdDb = new RrdDb(rrdDef);
                rrdDb.close();
                rrdDb = new RrdDb(rrdDef);
            }
            engine.addIndex(this);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkRrdDef(RrdDef expected, RrdDef actual) {
        if (expected.getDsCount() != actual.getDsCount()) {
            return false;
        }
        DsDef[] expectedDsDefs = expected.getDsDefs();
        DsDef[] actualDsDefs = actual.getDsDefs();

        for (int i = 0; i < expected.getDsCount(); i++) {
            DsDef expectedDS = expectedDsDefs[i];
            DsDef actualDsDef = actualDsDefs[i];

            if (!expectedDS.dump().equals(actualDsDef.dump())) {
                return false;
            }
        }

        if (expected.getArcCount() != actual.getArcCount()) {
            return false;
        }
        ArcDef[] expectedArcDefs = expected.getArcDefs();
        ArcDef[] actualArcDefs = actual.getArcDefs();

        for (int i = 0; i < expected.getArcCount(); i++) {
            ArcDef expectedDS = expectedArcDefs[i];
            ArcDef actualArcDef = actualArcDefs[i];

            if (!expectedDS.dump().equals(actualArcDef.dump())) {
                return false;
            }
        }

        return true;
    }

    protected RrdDef getRrdDef() {
        throw new RuntimeException("Not implemented");
    }

    public T getTimeSlot(long eventTime) {
        // timeslots are ringbuffer
        long l = (eventTime / slotDuration);
        int n = (int) l % NUM_OF_SLOT;

        T timeSlot = timeSlots.get(n);

        // if slot is from the past;
        if ((timeSlot != null) && (eventTime >= timeSlot.getEndTime())) {
            // try to remove from ring buffer
            if (timeSlots.compareAndSet(n, timeSlot, null)) {
                // we were successful. We need add this slot to flush queue
                slotsToFlush.add(timeSlot);
            }
            // reset current timeslot
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
            LOGGER.error("Skip event {} for index {}", eventTime, indexName);
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
            timeSlot.storeData(sample);
            if (rrdDb.getLastUpdateTime() >= currentTime) {
                new Object();
            }

            // TODO:
            // 13:06:22.316 [Timer-0] ERROR org.lex.perf.engine.EngineImpl - error
            //java.lang.IllegalArgumentException: Bad sample time: 1415869500. Last update time was 1415869515, at least one second step is required
            //at org.rrd4j.core.RrdDb.store(RrdDb.java:553) ~[RrdDb.class:na]
            //at org.rrd4j.core.Sample.update(Sample.java:197) ~[Sample.class:na]
            //at org.lex.perf.engine.Index.flush(Index.java:159) ~[Index.class:na]
            //at org.lex.perf.engine.Index.doSample(Index.java:207) ~[Index.class:na]
            //at org.lex.perf.engine.EngineImpl$1.run(EngineImpl.java:49) ~[EngineImpl$1.class:na]
            //at java.util.TimerThread.mainLoop(Timer.java:555) [na:1.7.0_45]
            //at java.util.TimerThread.run(Timer.java:505) [na:1.7.0_45]


            sample.update();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String getFileName() {
        return fileName;
    }

    public String getIndexName() {
        return indexName;
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
                //LOGGER.warn("dummy timeslot created {} ", slot.getStartTime() / 1000);
            } else {
                if (!timeSlots.compareAndSet(n, slot, null)) {
                    LOGGER.error("strange behavior");
                }
            }
            slotsToFlush.put(slot.getStartTime(), slot);
        }
        if (!sampleTime.compareAndSet(oldSampleTime, newSampleTime)) {
            LOGGER.error("strange behavior");
        }
        ;

        for (Map.Entry<Long, T> cts : slotsToFlush.entrySet()) {
            flush(cts.getValue());
        }
    }

    public int getSlotDuration() {
        return slotDuration;
    }

    @Override
    public Engine getEngine() {
        return engine;
    }
}