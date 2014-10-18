package org.lex.perf.impl;

import com.lmax.disruptor.RingBuffer;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.api.index.InspectionIndex;
import org.lex.perf.engine.Counter;
import org.lex.perf.engine.EngineImpl;
import org.lex.perf.engine.Index;
import org.lex.perf.util.ThreadUtil;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Stack;

/**
 * Created by Алексей on 17.09.2014.
 */
class InspectionIndexImpl extends IndexImpl implements InspectionIndex {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(InspectionIndexImpl.class);

    private final Counter counter;
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

    InspectionIndexImpl(IndexFactoryImpl indexFactory, EngineImpl engine, PerfIndexSeriesImpl indexSeries, String indexName) {
        super(indexFactory, engine, indexSeries, indexName);
        counter = new Counter(engine, indexSeries, indexName);
        perfIndexSeries = indexSeries;
    }

    @Override
    public Index getIndex() {
        return counter;
    }

    @Override
    public IndexType getIndexType() {
        return IndexType.INSPECTION;
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

        RingBuffer<PerfIndexSeriesImpl.IndexEvent> ringBuffer = perfIndexSeries.getDisruptor().getRingBuffer();
        long sequence = ringBuffer.next();
        PerfIndexSeriesImpl.IndexEvent event = ringBuffer.get(sequence);
        event.counter = counter;
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
                addDurationToParent(parent, childDuration1.count, childDuration1.duration, childDuration1.cpuDuration, childSeries[i]);
            }
            addDurationToParent(parent, 1, duration - childDuration, durationCPU - childCPUDuration, this.getIndexSeries().getName());
        }
        ringBuffer.publish(sequence);
    }

    private void addDurationToParent(InspectionElement parent, long count, long duration, long cpuDuration, String childSeries) {
        List<String> mapsTo = indexFactory.getMapsTo(childSeries);
        int idx = -1;
        for (String s : mapsTo) {
            for (int i = 0; i < parent.index.perfIndexSeries.getChildSeries().length; i++) {
                if (parent.index.perfIndexSeries.getChildSeries()[i].equals(s)) {
                    idx = i;
                    break;
                }
            }
        }
        if (idx == -1) {
            return;
        }
        Duration foundChild = parent.childDurations[idx];
        foundChild.count += count;
        foundChild.duration += duration;
        foundChild.cpuDuration += cpuDuration;
    }
}
