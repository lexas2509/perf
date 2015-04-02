package org.lex.perf.impl;

import org.lex.perf.api.factory.IndexType;
import org.lex.perf.api.index.Index;
import org.lex.perf.engine.EngineIndex;

/**
 * Created by Алексей on 17.09.2014.
 */
public abstract class IndexImpl implements Index {


    protected final String indexName;

    protected final PerfIndexSeriesImpl indexSeries;

    protected final IndexFactoryImpl indexFactory;

    private final IndexType indexType;

    public IndexImpl(IndexFactoryImpl indexFactory, PerfIndexSeriesImpl indexSeries, String indexName, IndexType indexType) {
        this.indexSeries = indexSeries;
        this.indexName = indexName;
        this.indexFactory = indexFactory;
        this.indexType = indexType;
    }

    public PerfIndexSeriesImpl getIndexSeries() {
        return indexSeries;
    }

    public abstract EngineIndex getIndex();

    public IndexType getIndexType() {
        return indexType;
    }

    @Override
    public String getName() {
        return indexName;
    }
}
