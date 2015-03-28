package org.lex.perf.api.factory;

import org.lex.perf.api.index.CounterIndex;
import org.lex.perf.api.index.InspectionIndex;

/**
 */
public abstract class IndexSeriesImpl {

    public static final String[] DEFAULT = new String[]{"total"};

    protected final String name;

    protected final IndexType indexType;


    protected IndexSeriesImpl(String name, IndexType indexType) {
        this.name = name;
        this.indexType = indexType;
    }

    public abstract void bindContext(String contextName);

    public abstract void unBindContext();
}
