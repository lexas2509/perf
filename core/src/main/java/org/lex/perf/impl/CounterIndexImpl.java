package org.lex.perf.impl;

import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.api.index.CounterIndex;
import org.lex.perf.engine.EngineImpl;
import org.lex.perf.engine.event.MonitoringEvent;

/**
 * Created by Алексей on 17.09.2014.
 */
class CounterIndexImpl extends IndexImpl implements CounterIndex {

    CounterIndexImpl(EngineImpl engine, IndexSeries indexSeries, String indexName) {
        super(engine, indexSeries, indexName);
    }

    @Override
    public void addRequest(long duration) {
        MonitoringEvent event = new MonitoringEvent();
        event.category = indexSeries;
        event.item = indexName;
        event.eventTime = System.currentTimeMillis();
        event.duration = duration;
        engine.putEvent(event);
    }

    @Override
    public IndexType getIndexType() {
        return IndexType.COUNTER;
    }
}
