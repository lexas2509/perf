package org.lex.perf.impl;

import org.lex.perf.api.factory.IndexType;
import org.lex.perf.engine.EngineImpl;
import org.lex.perf.engine.Gauge;
import org.lex.perf.engine.GaugeTimeSlot;
import org.lex.perf.engine.Index;

/**
 * Created by Алексей on 17.09.2014.
 */
public class GaugeIndexImpl extends IndexImpl {

    private final Gauge gauge;

    public GaugeIndexImpl(EngineImpl engine, PerfIndexSeriesImpl indexSeries, String indexName) {
        super(engine, indexSeries, indexName);
        gauge = new Gauge(engine, indexSeries, indexName);
    }

    @Override
    public IndexType getIndexType() {
        return IndexType.GAUGE;
    }

    public void putSensorValue(long eventTime, double value) {
        GaugeTimeSlot timeSlot = gauge.getTimeSlot(eventTime);
        timeSlot.setValue(value);
    }

    @Override
    public Index getIndex() {
        return gauge;
    }
}
