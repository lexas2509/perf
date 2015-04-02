package org.lex.perf.impl;

import com.lmax.disruptor.RingBuffer;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.engine.Engine;
import org.lex.perf.engine.EngineIndex;
import org.lex.perf.engine.IndexEvent;

import java.math.BigDecimal;

/**
 * Created by Алексей on 17.09.2014.
 */
public class GaugeIndexImpl extends IndexImpl {

    private final EngineIndex gauge;

    public GaugeIndexImpl(IndexFactoryImpl indexFactory, Engine engine, PerfIndexSeriesImpl indexSeries, String indexName) {
        super(indexFactory, indexSeries, indexName, IndexType.GAUGE);
        gauge = engine.getGauge(indexName, indexSeries.getName());
    }

    public void putSensorValue(long eventTime, BigDecimal value) {
        RingBuffer<IndexEvent> ringBuffer = indexSeries.getDisruptor().getRingBuffer();
        long sequence = ringBuffer.next();
        IndexEvent event = ringBuffer.get(sequence);
        event.engineIndex = gauge;
        event.requestTime = eventTime;
        event.value = value;
        ringBuffer.publish(sequence);
    }

    @Override
    public EngineIndex getIndex() {
        return gauge;
    }

}
