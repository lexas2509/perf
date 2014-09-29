package org.lex.perf.impl;

import org.lex.perf.api.factory.IndexType;
import org.lex.perf.api.index.CounterIndex;
import org.lex.perf.engine.Counter;
import org.lex.perf.engine.CounterTimeSlot;
import org.lex.perf.engine.EngineImpl;
import org.lex.perf.engine.Index;

/**
 * Created by Алексей on 17.09.2014.
 */
class CounterIndexImpl extends IndexImpl implements CounterIndex {

    private final Counter counter;

    CounterIndexImpl(IndexFactoryImpl indexFactory, EngineImpl engine, PerfIndexSeriesImpl indexSeries, String indexName) {
        super(indexFactory, engine, indexSeries, indexName);
        counter = new Counter(engine, indexSeries, indexName);
    }

    @Override
    public void addRequest(long requestTime, long[] duration) {
        CounterTimeSlot timeSlot = counter.getTimeSlot(requestTime);
        Duration dur = new Duration();
        dur.duration = duration[0];
        dur.cpuDuration = duration[1];
        timeSlot.addHit(dur, null);
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
