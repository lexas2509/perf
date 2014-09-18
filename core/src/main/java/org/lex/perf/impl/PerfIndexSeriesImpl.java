package org.lex.perf.impl;

import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.factory.IndexSeriesImpl;
import org.lex.perf.api.factory.IndexType;

/**
 * Created by Алексей on 18.09.2014.
 */
public class PerfIndexSeriesImpl extends IndexSeriesImpl {

    private IndexSeries[] childSeries;
    private boolean supportHistogramm;

    public PerfIndexSeriesImpl(String name, IndexType indexType) {
        super(name, indexType);
        childSeries = null;
        supportHistogramm = true;
    }

    public IndexSeries[] getChildSeries() {
        if (childSeries == null) {
            return IndexFactory.getIndexSeries();
        }
        return childSeries;
    }

    public boolean isSupportCPU() {
        return true;
    }

    public boolean isSupportHistogramm() {
        return supportHistogramm;
    }
}
