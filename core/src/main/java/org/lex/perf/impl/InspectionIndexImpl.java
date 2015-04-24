package org.lex.perf.impl;

import com.lmax.disruptor.RingBuffer;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.api.index.InspectionIndex;
import org.lex.perf.engine.Duration;
import org.lex.perf.engine.Engine;
import org.lex.perf.engine.EngineIndex;
import org.lex.perf.engine.IndexEvent;
import org.lex.perf.util.ThreadUtil;
import org.slf4j.LoggerFactory;

import java.util.Stack;

/**
 * Created by Алексей on 17.09.2014.
 */
class InspectionIndexImpl extends IndexImpl implements InspectionIndex {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(InspectionIndexImpl.class);

    private final EngineIndex counter;
    private final PerfIndexSeriesImpl perfIndexSeries;

    private static class InspectionElement {
        long start;
        long startCPU;

        Duration[] childDurations;

        public InspectionIndexImpl index;
    }

    private static ThreadLocal<Stack<InspectionElement>> INSPECTIONS = new ThreadLocal<Stack<InspectionElement>>() {
        @Override
        protected Stack<InspectionElement> initialValue() {
            return new Stack<InspectionElement>();
        }
    };

    InspectionIndexImpl(IndexFactoryImpl indexFactory, Engine engine, PerfIndexSeriesImpl indexSeries, String indexName) {
        super(indexFactory, indexSeries, indexName, IndexType.INSPECTION);
        counter = engine.getCounter(indexName, indexSeries.getName(), IndexType.INSPECTION, indexSeries.isSupportCPU(), indexSeries.isSupportHistogramm(), indexSeries.getChildSeries());
        perfIndexSeries = indexSeries;
    }

    @Override
    public EngineIndex getIndex() {
        return counter;
    }

    @Override
    public void bindContext() {
        InspectionElement current = new InspectionElement();
        current.start = System.nanoTime();
        current.startCPU = ThreadUtil.getCurrentThreadCpuTime();
        current.index = this;
        int length = perfIndexSeries.getChildSeries().length;
        current.childDurations = new Duration[length];
        for (int i = 0; i < length; i++) {
            current.childDurations[i] = new Duration();
        }
        INSPECTIONS.get().push(current);
    }

    @Override
    public void unBindContext() {
        Stack<InspectionElement> inspections = InspectionIndexImpl.INSPECTIONS.get();
        InspectionElement current = inspections.pop();
        InspectionElement parent = inspections.empty() ? null : inspections.peek();

        if (!this.equals(current.index)) {
            LOGGER.error("Incorrect unbinding");
        }
        long finish = System.nanoTime();
        long duration = finish - current.start;
        long durationCPU = ThreadUtil.getCurrentThreadCpuTime() - current.startCPU;
        long requestTime = System.currentTimeMillis();

        RingBuffer<IndexEvent> ringBuffer = perfIndexSeries.getDisruptor().getRingBuffer();
        long sequence = ringBuffer.next();
        IndexEvent event = ringBuffer.get(sequence);
        event.engineIndex = counter;
        event.requestTime = requestTime;
        event.own.count = 1;

        long childDuration = 0;
        long childCPUDuration = 0;
        Duration[] childDurations = current.childDurations;
        for (int i = 0; i < childDurations.length; i++) {
            Duration it = childDurations[i];
            event.childsDurations[i].count = it.count;
            event.childsDurations[i].cpuDuration = it.cpuDuration;
            event.childsDurations[i].duration = it.duration;
            childDuration += it.duration;
            childCPUDuration += it.duration;
        }

        event.own.duration = duration;
        event.own.cpuDuration = durationCPU;

        // Заполняем данные parent-ового контекста
        if (parent != null) {
            String[] childSeries = this.perfIndexSeries.getChildSeries();
            for (int i = 0; i < childDurations.length; i++) {
                Duration childDuration1 = childDurations[i];
                addDurationToParent(parent, childDuration1.count, childDuration1.duration, childDuration1.cpuDuration, i);
            }
            addDurationToParent(parent, 1, duration - childDuration, durationCPU - childCPUDuration, this.getIndexSeries().getIndex());
        }
        ringBuffer.publish(sequence);
    }

    private void addDurationToParent(InspectionElement parent, long count, long duration, long cpuDuration, int childIndex) {
        int idx = parent.index.perfIndexSeries.mapsTo(childIndex);
        if (idx == -1) {
            return;
        }
        Duration foundChild = parent.childDurations[idx];
        foundChild.count += count;
        foundChild.duration += duration;
        foundChild.cpuDuration += cpuDuration;
    }

}
