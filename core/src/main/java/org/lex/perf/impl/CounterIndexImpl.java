package org.lex.perf.impl;

import com.lmax.disruptor.RingBuffer;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.api.index.CounterIndex;
import org.lex.perf.engine.EngineIndex;
import org.lex.perf.engine.Engine;
import org.lex.perf.engine.IndexEvent;

/**
 * Created by Алексей on 17.09.2014.
 */
class CounterIndexImpl extends IndexImpl implements CounterIndex {

    private final EngineIndex counter;

    CounterIndexImpl(IndexFactoryImpl indexFactory, Engine engine, PerfIndexSeriesImpl indexSeries, String indexName) {
        super(indexFactory, indexSeries, indexName, IndexType.COUNTER);

        counter = engine.getCounter(indexName, indexSeries.getName(), IndexType.COUNTER, indexSeries.isSupportCPU(), indexSeries.isSupportHistogramm(),
                indexSeries.getChildSeries());

    }

    @Override
    public void addRequest(long requestTime, long own, long[] duration) {
        RingBuffer<IndexEvent> ringBuffer = indexSeries.getDisruptor().getRingBuffer();
        long sequence = ringBuffer.next();
        IndexEvent event = ringBuffer.get(sequence);
        event.engineIndex = counter;
        event.requestTime = requestTime;
        event.own.count = 1;
        event.own.duration = own;
        ringBuffer.publish(sequence);
    }

    @Override
    public EngineIndex getIndex() {
        return counter;
    }
}
