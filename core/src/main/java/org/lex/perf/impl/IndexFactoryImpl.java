package org.lex.perf.impl;

import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.index.GaugeIndex;
import org.lex.perf.api.index.Index;
import org.lex.perf.engine.EngineImpl;
import org.lex.perf.sensor.SensorEngine;

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
                    index = createIndex(indexSeries, indexName);
                    categoryIndices.put(indexName, index);
                }
            }
        }
        return index;
    }

    private Index createIndex(IndexSeries indexSeries, String indexName) {
        switch (indexSeries.getIndexType()) {
            case INSPECTION:
                return new InspectionIndexImpl(engine, indexSeries, indexName);
            case COUNTER:
                return new CounterIndexImpl(engine, indexSeries, indexName);
            default:
                return null;
        }
    }

    @Override
    public void registerGauge(GaugeIndex gaugeIndex) {
        sensorEngine.addGaugeSensor(gaugeIndex);
    }

    public EngineImpl getEngine() {
        return engine;
    }
}
