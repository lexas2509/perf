package org.lex.perf.api.factory;

import org.lex.perf.api.index.CPUCounterIndex;
import org.lex.perf.api.index.CounterIndex;
import org.lex.perf.api.index.InspectionIndex;

/**
 */
public class IndexSeriesImpl implements IndexSeries {

    private final String name;

    private final IndexType indexType;


    protected IndexSeriesImpl(String name, IndexType indexType) {
        this.name = name;
        this.indexType = indexType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IndexType getIndexType() {
        return indexType;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void addRequest(String indexName, long eventTime, long duration) {
        org.lex.perf.api.index.Index index = IndexFactory.getFactory().getIndex(this, indexName);
        ((CounterIndex) index).addRequest(eventTime, duration);
    }

    @Override
    public void addRequest(String indexName, long eventTime, long duration, long cpuDuration) {
        org.lex.perf.api.index.Index index = IndexFactory.getFactory().getIndex(this, indexName);
        ((CPUCounterIndex) index).addRequest(eventTime, duration, cpuDuration);
    }

    @Override
    public void bindContext(String contextName) {
        org.lex.perf.api.index.Index index = IndexFactory.getFactory().getIndex(this, contextName);
        ((InspectionIndex) index).bindContext();
    }

    @Override
    public void unbindContext(String contextName) {
        org.lex.perf.api.index.Index index = IndexFactory.getFactory().getIndex(this, contextName);
        ((InspectionIndex) index).bindContext();
    }
}
