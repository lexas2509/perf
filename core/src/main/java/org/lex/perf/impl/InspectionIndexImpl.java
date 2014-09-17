package org.lex.perf.impl;

import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.index.InspectionIndex;
import org.lex.perf.engine.EngineImpl;

import java.util.Stack;

/**
 * Created by Алексей on 17.09.2014.
 */
class InspectionIndexImpl extends CounterIndexImpl implements InspectionIndex {

    private ThreadLocal<Stack<Long>> start = new ThreadLocal<Stack<Long>>() {
        @Override
        protected Stack<Long> initialValue() {
            return new Stack<Long>();
        }
    };

    InspectionIndexImpl(EngineImpl engine, IndexSeries indexSeries, String indexName) {
        super(engine, indexSeries, indexName);
    }

    @Override
    public void bindContext() {
        start.get().push(System.currentTimeMillis());
    }

    @Override
    public void unBindContext() {
        long finish = System.currentTimeMillis();
        long duration = finish - start.get().pop();
        addRequest(duration);
    }
}
