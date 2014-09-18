package org.lex.perf.impl;

import org.lex.perf.api.factory.IndexType;
import org.lex.perf.api.index.CPUCounterIndex;
import org.lex.perf.engine.Counter;
import org.lex.perf.engine.CounterTimeSlot;
import org.lex.perf.engine.EngineImpl;
import org.lex.perf.engine.Index;

/**
 * Created by Алексей on 18.09.2014.
 */
public class CPUCounterIndexImpl extends IndexImpl implements CPUCounterIndex {

    private final Counter counter;

    CPUCounterIndexImpl(EngineImpl engine, PerfIndexSeriesImpl indexSeries, String indexName) {
        super(engine, indexSeries, indexName);
        counter = new Counter(engine, indexSeries, indexName);
    }

    @Override
    public void addRequest(long requestTime, long duration, long cpuDuration) {
        CounterTimeSlot timeSlot = counter.getTimeSlot(requestTime);
        timeSlot.addHit(duration / 1000 / 1000, cpuDuration);
    }

    @Override
    public IndexType getIndexType() {
        return IndexType.CPUCOUNTER;
    }

    @Override
    public Index getIndex() {
        return counter;
    }
}

