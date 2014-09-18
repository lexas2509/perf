package org.lex.perf.impl;

import org.lex.perf.api.index.InspectionIndex;
import org.lex.perf.engine.EngineImpl;
import org.lex.perf.util.ThreadUtil;

import java.util.Stack;

/**
 * Created by Алексей on 17.09.2014.
 */
class InspectionIndexImpl extends CounterIndexImpl implements InspectionIndex {

    private static class InspectionElement {
        long start;
        long startCPU;
        public String name;
    }

    private ThreadLocal<Stack<InspectionElement>> start = new ThreadLocal<Stack<InspectionElement>>() {
        @Override
        protected Stack<InspectionElement> initialValue() {
            return new Stack<InspectionElement>();
        }
    };

    InspectionIndexImpl(EngineImpl engine, PerfIndexSeriesImpl indexSeries, String indexName) {
        super(engine, indexSeries, indexName);
    }

    @Override
    public void bindContext() {
        InspectionElement current = new InspectionElement();
        current.start = System.nanoTime();
        current.startCPU = ThreadUtil.getCurrentThreadCpuTime();
        current.name = indexName;
        start.get().push(current);
    }

    @Override
    public void unBindContext() {
        InspectionElement c = start.get().pop();
        long finish = System.nanoTime();
        long duration = finish - c.start;
        long durationCPU = ThreadUtil.getCurrentThreadCpuTime() - c.startCPU;
        addRequest(System.currentTimeMillis(), new long[]{duration, durationCPU});
    }
}
