package org.lex.perf.engine;

import org.rrd4j.core.Sample;

/**
 * To change this template use File | Settings | File Templates.
 */
public class CounterTimeSlot  extends TimeSlot {
    private final static long[] times = {0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384};
    private final static int CNT = times.length;

    private long count = 0;
    private long max = Long.MIN_VALUE;
    private long total = 0;
    private long[] statTimes = new long[CNT + 1];

    public CounterTimeSlot(long startTime, long endTime) {
        super(startTime, endTime);
    }

    public synchronized void addHit(long time) {
        count++;
        total = total + time;
        if (time > max) {
            max = time;
        }

        // Переведем в ms

        time = time / 1000;
        int w = CNT / 2;
        int pos = CNT / 2;
        while (w > 1) {
            w = w / 2;
            if (times[pos] >= time) {
                pos = pos - w;
            } else {
                pos = pos + w;
            }
        }
        if (times[pos] >= time) {
            pos--;
        }
        statTimes[pos]++;
    }

    public long getStatCount(int idx) {
        return statTimes[idx];
    }

    public long getCount() {
        return count;
    }

    public long getMax() {
        return max;
    }

    public long getTotal() {
        return total;
    }


    @Override
    public void storeData(Sample sample) {
        sample.setValue("hits", getCount());
        sample.setValue("value", getTotal());
    }
}
