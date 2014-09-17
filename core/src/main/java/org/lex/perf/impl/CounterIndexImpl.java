package org.lex.perf.impl;

import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.api.index.CounterIndex;
import org.lex.perf.engine.Counter;
import org.lex.perf.engine.CounterTimeSlot;
import org.lex.perf.engine.EngineImpl;
import org.lex.perf.engine.Index;
import org.lex.perf.engine.event.MonitoringEvent;

/**
 * Created by Алексей on 17.09.2014.
 */
class CounterIndexImpl extends IndexImpl implements CounterIndex {

    private final Counter counter;

    CounterIndexImpl(EngineImpl engine, IndexSeries indexSeries, String indexName) {
        super(engine, indexSeries, indexName);
        counter = new Counter(engine, indexSeries, indexName);
    }

    @Override
    public void addRequest(long duration) {
        MonitoringEvent event = new MonitoringEvent();
        event.category = indexSeries;
        event.item = indexName;
        event.eventTime = System.currentTimeMillis();
        event.duration = duration;

        CounterTimeSlot timeSlot = counter.getTimeSlot(event.eventTime);
        timeSlot.addHit(event.duration / 1000 / 1000);
    }

    @Override
    public IndexType getIndexType() {
        return IndexType.COUNTER;
    }

    @Override
    public Index getIndex() {
        return counter;
    }
}
