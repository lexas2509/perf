package org.lex.perf.engine;

import org.rrd4j.core.Sample;

/**
 * To change this template use File | Settings | File Templates.
 */
public class CounterTimeSlot extends TimeSlot {
    public final static long[] times = {0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384};
    private final static int CNT = times.length;
    private final boolean supportCPU;
    private final boolean supportHistogramm;

    private long count = 0;
    private long max = Long.MIN_VALUE;
    private long total = 0;
    private long totalCPU = 0;
    private long[] statTimes = new long[CNT + 1];

    public CounterTimeSlot(long startTime, long endTime, boolean supportCPU, boolean supportHistogramm) {
        super(startTime, endTime);
        this.supportCPU = supportCPU;
        this.supportHistogramm = supportHistogramm;
    }

    public synchronized void addHit(long time, long cpuDuration) {
        count++;
        total = total + time;
        totalCPU = totalCPU + cpuDuration;
        if (time > max) {
            max = time;
        }

        // Переведем в ms

        time = time;
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
        int size = 2 + (supportCPU ? 1 : 0) + (supportHistogramm ? statTimes.length : 0);
        double[] t = new double[size];
        int idx = 0;
        t[idx++] = getCount();
        t[idx++] = getTotal();


        if (supportCPU) {
            t[idx++] = getTotalCPU();
        }

        if (supportHistogramm) {
            for (int i = 0; i < statTimes.length; i++) {
                t[i + idx] = statTimes[i];
            }
        }

        sample.setValues(t);
    }

    public double getTotalCPU() {
        return totalCPU;
    }
}
