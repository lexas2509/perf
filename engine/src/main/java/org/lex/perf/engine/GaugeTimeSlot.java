package org.lex.perf.engine;

import org.rrd4j.core.Sample;

/**
 */
public class GaugeTimeSlot extends TimeSlot {

    private double value;

    public GaugeTimeSlot(long startTime, long endTime) {
        super(startTime, endTime);
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public void storeData(Sample sample) {
        sample.setValue("value", getValue());
    }
}

