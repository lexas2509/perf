package org.lex.perf.impl;

import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.engine.EngineImpl;
import org.lex.perf.engine.Gauge;
import org.lex.perf.engine.GaugeTimeSlot;
import org.lex.perf.engine.Index;
import org.lex.perf.engine.event.MonitoringValue;

/**
 * Created by Алексей on 17.09.2014.
 */
public class GaugeIndexImpl extends IndexImpl {

    private final Gauge gauge;

    public GaugeIndexImpl(EngineImpl engine, IndexSeries indexSeries, String indexName) {
        super(engine, indexSeries, indexName);
        gauge = new Gauge(engine, indexSeries, indexName);
    }

    @Override
    public IndexType getIndexType() {
        return IndexType.GAUGE;
    }

    public void putSensorValue(MonitoringValue event) {
        GaugeTimeSlot timeSlot = gauge.getTimeSlot(event.eventTime);
        timeSlot.setValue(event.value);
    }

    @Override
    public Index getIndex() {
        return gauge;
    }
}
