package org.lex.perf.api.index;

import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.factory.IndexType;

/**
 * Created by Алексей on 28.03.2015.
 */
public abstract class GaugeSensorImpl implements GaugeIndex {
    private IndexSeries indexSeries;

    public IndexType getIndexType() {
        return IndexType.GAUGE;
    }

    public void setIndexSeries(IndexSeries indexSeries) {
        this.indexSeries = indexSeries;
    }

    public IndexSeries getIndexSeries() {
        return indexSeries;
    }
}
