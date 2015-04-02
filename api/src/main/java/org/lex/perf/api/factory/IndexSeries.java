package org.lex.perf.api.factory;

/**
 * Created by Алексей on 18.09.2014.
 */
public final class IndexSeries {

    private final String name;

    private IndexSeriesImpl impl;
    private boolean active;

    public IndexSeries(String name) {
        this.name = name;
    }

    void configure(IndexSeriesImpl impl, boolean active) {
        this.impl = impl;
        this.active = active;
    }

    public String getName() {
        return name;
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
