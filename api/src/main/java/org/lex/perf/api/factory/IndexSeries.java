package org.lex.perf.api.factory;

/**
 * Created by Алексей on 18.09.2014.
 */
public final class IndexSeries {

    private final String name;

    private final IndexType indexType;

    private IndexSeriesImpl impl;
    private boolean active;

    public IndexSeries(IndexType indexType, String name) {
        this.indexType = indexType;
        this.name = name;
    }

    void configure(IndexSeriesImpl impl, boolean active) {
        this.impl = impl;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public void bindContext(String contextName) {
        impl.bindContext(contextName);
    }

    public void unBindContext() {
        impl.unBindContext();
    }

    public boolean isActive() {
        return active;
    }

    IndexSeriesImpl getImpl() {
        return impl;
    }
}
