package org.lex.perf.engine;

import org.rrd4j.core.Sample;

/**
 */
public abstract class TimeSlot {
    private long endTime;
    private long startTime;

    public TimeSlot(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public abstract void storeData(Sample sample);
}
