package org.lex.perf.impl;

import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.factory.IndexSeriesImpl;
import org.lex.perf.api.index.Index;
import org.lex.perf.engine.EngineImpl;

/**
 * Created by Алексей on 17.09.2014.
 */
public abstract class IndexImpl implements Index {

    protected final EngineImpl engine;

    protected final String indexName;

    protected final PerfIndexSeriesImpl indexSeries;

    protected final IndexFactoryImpl indexFactory;

    public IndexImpl(IndexFactoryImpl indexFactory, EngineImpl engine, PerfIndexSeriesImpl indexSeries, String indexName) {
        this.engine = engine;
        this.indexSeries = indexSeries;
        this.indexName = indexName;
        this.indexFactory = indexFactory;
    }

    public PerfIndexSeriesImpl getIndexSeries() {
        return indexSeries;
    }

    public abstract org.lex.perf.engine.Index getIndex();

    protected String getFileName() {
        return indexFactory.getCategoryPrefix(indexSeries) + "-" + engine.getIndexFileName(indexSeries.getName(), indexName);
    }

}
