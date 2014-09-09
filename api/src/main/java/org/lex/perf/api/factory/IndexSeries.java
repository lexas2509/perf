package org.lex.perf.api.factory;

import org.lex.perf.api.index.CounterIndex;

/**
 */
public class IndexSeries {

    private final String name;

    private final IndexType indexType;


    public IndexSeries(String name, IndexType indexType) {
        this.name = name;
        this.indexType = indexType;
    }

    public String getName() {
        return name;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public boolean isActive() {
        return true;
    }

    public void addRequest(String indexName, long duration) {
        org.lex.perf.api.index.Index index = IndexFactory.getFactory().getIndex(this, indexName);
        ((CounterIndex) index).addRequest(duration);
    }

    public void bindContext(String contextName) {
        // not implemented yet. Currently it doesn't support contexts.
    }

    public void unbindContext(String contextName) {
        // not implemented yet. Currently it doesn't support contexts.
    }
}
