package org.lex.perf.impl;

import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.api.index.GaugeIndex;
import org.lex.perf.api.index.Index;
import org.lex.perf.engine.EngineImpl;
import org.lex.perf.sensor.SensorEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public class IndexFactoryImpl implements IndexFactory.IIndexFactory {

    private SensorEngine sensorEngine;

    private final EngineImpl engine;

    private Map<String, Map<String, Index>> indexes = new ConcurrentHashMap<String, Map<String, Index>>();

    public IndexFactoryImpl() {
        engine = new EngineImpl();
        sensorEngine = new SensorEngine(engine);

    }

    @Override
    public Index getIndex(IndexSeries indexSeries, String indexName) {
        Map<String, Index> categoryIndices = indexes.get(indexSeries.getName());
        if (categoryIndices == null) {
            synchronized (this) {
                categoryIndices = indexes.get(indexSeries.getName());
                if (categoryIndices == null) {
                    categoryIndices = new ConcurrentHashMap<String, Index>();
                    indexes.put(indexSeries.getName(), categoryIndices);
                }
            }
        }
        Index index = categoryIndices.get(indexName);
        if (index == null) {
            synchronized (categoryIndices) {
                index = categoryIndices.get(indexName);
                if (index == null) {
                    index = createIndex((PerfIndexSeriesImpl) indexSeries, indexName);
                    categoryIndices.put(indexName, index);
                }
            }
        }
        return index;
    }

    private Index createIndex(PerfIndexSeriesImpl indexSeries, String indexName) {
        switch (indexSeries.getIndexType()) {
            case INSPECTION:
                return new InspectionIndexImpl(engine, indexSeries, indexName);
            case COUNTER:
                return new CounterIndexImpl(engine, indexSeries, indexName);
            case CPUCOUNTER:
                return new CPUCounterIndexImpl(engine, indexSeries, indexName);
            case GAUGE:
                return new GaugeIndexImpl(engine, indexSeries, indexName);
            default:
                throw new RuntimeException("Unknown indexSeries.");
        }
    }

    @Override
    public void registerGauge(GaugeIndex gaugeIndex) {
        String[] indexItems = gaugeIndex.getItems();
        GaugeIndexImpl[] gaugeIndexes = new GaugeIndexImpl[indexItems.length];
        int idx = 0;
        for (String indexItem : indexItems) {
            gaugeIndexes[idx] = (GaugeIndexImpl) getIndex(gaugeIndex.getIndexSeries(), indexItem);
            idx++;
        }
        sensorEngine.addGaugeSensor(gaugeIndex, gaugeIndexes);
    }

    public List<Index> getIndexes(IndexSeries category) {
        Map<String, Index> indexes = this.indexes.get(category.getName());
        if (indexes == null) {
            return Collections.emptyList();
        }
        return new ArrayList<Index>(indexes.values());
    }

    @Override
    public IndexSeries createIndexSeries(String indexSeriesName, IndexType indexType) {
        return new PerfIndexSeriesImpl(indexSeriesName, indexType);
    }

    public EngineImpl getEngine() {
        return engine;
    }
}
