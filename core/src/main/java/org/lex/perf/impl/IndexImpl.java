package org.lex.perf.impl;

import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.index.Index;
import org.lex.perf.engine.EngineImpl;

/**
 * Created by Алексей on 17.09.2014.
 */
public abstract class IndexImpl implements Index {

    protected final EngineImpl engine;

    protected final String indexName;

    protected final IndexSeries indexSeries;

    protected boolean isActive = true;

    public IndexImpl(EngineImpl engine, IndexSeries indexSeries, String indexName) {
        this.engine = engine;
        this.indexSeries = indexSeries;
        this.indexName = indexName;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }


    @Override
    public IndexSeries getIndexSeries() {
        return indexSeries;
    }

    public abstract org.lex.perf.engine.Index getIndex();

}
