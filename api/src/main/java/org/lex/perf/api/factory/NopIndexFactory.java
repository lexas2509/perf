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
    public Index getIndex(final IndexSeries indexSeries, String indexName) {
        switch (indexSeries.getIndexType()) {
            case INSPECTION:
                return new InspectionIndex() {
                    @Override
                    public void bindContext() {

                    }

                    @Override
                    public void unBindContext() {

                    }

                    @Override
                    public boolean isActive() {
                        return false;
                    }

                    @Override
                    public IndexType getIndexType() {
                        return indexSeries.getIndexType();
                    }

                    @Override
                    public IndexSeries getIndexSeries() {
                        return indexSeries;
                    }
                };
            case GAUGE:
                return new GaugeIndex() {
                    @Override
                    public BigDecimal[] getValues() {
                        return new BigDecimal[0];
                    }

                    @Override
                    public String[] getItems() {
                        return new String[0];
                    }

                    @Override
                    public boolean isActive() {
                        return false;
                    }

                    @Override
                    public IndexType getIndexType() {
                        return indexSeries.getIndexType();
                    }

                    @Override
                    public IndexSeries getIndexSeries() {
                        return indexSeries;
                    }
                };
            case COUNTER:
                return new CounterIndex() {
                    @Override
                    public void addRequest(long requestTime, long[] durations) {

                    }

                    @Override
                    public boolean isActive() {
                        return false;
                    }

                    @Override
                    public IndexType getIndexType() {
                        return indexSeries.getIndexType();
                    }

                    @Override
                    public IndexSeries getIndexSeries() {
                        return indexSeries;
                    }
                };
            default:
                throw new RuntimeException("Unknown index type");
        }
    }

    @Override
    public void registerGauge(GaugeIndex gaugeIndex) {
    }

    @Override
    public IndexSeries createIndexSeries(String indexSeriesName, IndexType indexType) {
        return new IndexSeriesImpl(indexSeriesName, indexType);
    }
}
