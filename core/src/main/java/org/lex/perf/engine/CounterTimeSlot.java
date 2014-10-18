package org.lex.perf.engine;

import org.lex.perf.impl.Duration;
import org.rrd4j.core.Sample;

/**
 * To change this template use File | Settings | File Templates.
 */
public class CounterTimeSlot extends TimeSlot {
    public final static long[] times = {0, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384};
    private final static int CNT = times.length;
    private final boolean supportCPU;
    private final boolean supportHistogramm;
    private final String[] childsSeries;

    private long max = Long.MIN_VALUE;
    private Duration duration;
    private Duration[] childDurations;
    private long[] statTimes = new long[CNT];

    public CounterTimeSlot(long startTime, long endTime, boolean supportCPU, boolean supportHistogramm, String[] childSeries) {
        super(startTime, endTime);
        this.supportCPU = supportCPU;
        this.supportHistogramm = supportHistogramm;
        this.childsSeries = childSeries;
        duration = new Duration();
        childDurations = new Duration[childSeries.length];
        for (int i = 0; i < childSeries.length; i++) {
            childDurations[i] = new Duration();
        }
    }

    public void addHit(long duration) {
        Duration own = new Duration();
        own.duration = duration * 1000 * 1000;
        addHit(own, null);
    }

    public void addHit(Duration own, Duration[] childs) {
        duration.count++;
        duration.duration = duration.duration + own.duration;
        if (supportCPU) {
            duration.cpuDuration = duration.cpuDuration + own.cpuDuration;
        }

        if (own.duration > max) {
            max = own.duration;
        }

        // Переведем в ms и заполним histogram
        if (supportHistogramm) {
            double duration = own.duration / 1000 / 1000;
            int w = CNT / 2;
            int pos = CNT / 2;
            while (w > 1) {
                w = w / 2;
                if (times[pos] >= duration) {
                    pos = pos - w;
                } else {
                    pos = pos + w;
                }
            }
            if (times[pos] >= duration) {
                pos--;
            }
            statTimes[pos]++;
        }

        // сохраним данные по вложенным сериям
        if ((childsSeries.length > 0) && (childDurations != null) && (childs != null)) {
            for (int i = 0; i < childsSeries.length; i++) {
                Duration childDuration = childDurations[i];
                Duration child = childs[i];
                if (childDuration != null && child != null) {
                    childDuration.count += child.count;
                    childDuration.cpuDuration += child.cpuDuration;
                    childDuration.duration += child.duration;
                }
            }
        }
    }

    public long getStatCount(int idx) {
        return statTimes[idx];
    }


    @Override
    public void storeData(Sample sample) {
        int size = 3 + (supportHistogramm ? statTimes.length : 0) + 3 * childDurations.length;
        double[] t = new double[size];
        int idx = 0;
        t[idx++] = duration.count;
        t[idx++] = duration.duration;


        t[idx++] = duration.cpuDuration;

        if (supportHistogramm) {
            for (int i = 0; i < statTimes.length; i++) {
                t[idx++] = statTimes[i];
            }
        }

        for (int i = 0; i < childDurations.length; i++) {
            t[idx++] = childDurations[i].count;
            t[idx++] = childDurations[i].duration;
            t[idx++] = childDurations[i].cpuDuration;

        }

        sample.setValues(t);
    }
}
