package org.lex.perf.api.factory;

import org.lex.perf.api.index.CounterIndex;
import org.lex.perf.api.index.GaugeIndex;
import org.lex.perf.api.index.Index;
import org.lex.perf.api.index.InspectionIndex;

import java.math.BigDecimal;

/**
 * Created by Алексей on 18.09.2014.
 */
public class NopIndexFactory implements IndexFactory.IIndexFactory {

    @Override
    public void registerGauge(IndexSeriesImpl indexSeriesImpl, GaugeIndex gaugeIndex) {
    }

    @Override
    public IndexSeriesImpl createIndexSeriesImpl(String indexSeriesName, IndexType indexType) {
        return new IndexSeriesImpl(indexSeriesName, indexType) {
            @Override
            public void bindContext(String contextName) {

            }

            @Override
            public void unBindContext() {

            }
        };
    }
}
