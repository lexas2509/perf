package org.lex.perf.impl;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexSeriesImpl;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.api.index.GaugeIndex;
import org.lex.perf.api.index.Index;
import org.lex.perf.config.*;
import org.lex.perf.engine.Engine;
import org.lex.perf.engine.EngineFactory;
import org.lex.perf.engine.IndexEvent;
import org.lex.perf.sensor.SensorEngine;
import org.lex.perf.util.JAXBUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 */
public class IndexFactoryImpl implements IndexFactory.IIndexFactory {

    public static final int MAX_CHILD_SERIES_LENGTH = 3;

    private final java.util.concurrent.Executor executor;

    private final Disruptor<IndexEvent> disruptor;

    private SensorEngine sensorEngine;

    private final Engine engine;

    private Map<String, Map<String, Index>> indexes = new ConcurrentHashMap<String, Map<String, Index>>();

    private final Config config;

    public IndexFactoryImpl() {
        String contextPath = "org.lex.perf.config";
        config = JAXBUtil.getObject(contextPath, "defaultConfig.xml");
        engine = EngineFactory.getEngine();
        sensorEngine = new SensorEngine(engine);

        // create asynchronous handler (based on disruptor)
        executor = Executors.newSingleThreadExecutor();
        disruptor = new Disruptor<IndexEvent>(INDEX_EVENT_FACTORY,
                16384, executor, ProducerType.MULTI, new SleepingWaitStrategy());
        disruptor.handleEventsWith(engine.getHandler());
        disruptor.start();

    }

    public final EventFactory<IndexEvent> INDEX_EVENT_FACTORY = new EventFactory<IndexEvent>() {
        @Override
        public IndexEvent newInstance() {
            return new IndexEvent(MAX_CHILD_SERIES_LENGTH);
        }
    };



    public Index getIndex(PerfIndexSeriesImpl indexSeries, String indexName, IndexType indexType) {
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
                    index = createIndex(indexSeries, indexName, indexType);
                    categoryIndices.put(indexName, index);
                }
            }
        }
        return index;
    }

    private Index createIndex(PerfIndexSeriesImpl indexSeries, String indexName, IndexType indexType) {
        switch (indexType) {
            case INSPECTION:
                return new InspectionIndexImpl(this, engine, indexSeries, indexName);
            case COUNTER:
                return new CounterIndexImpl(this, engine, indexSeries, indexName);
            case GAUGE:
                return new GaugeIndexImpl(this, engine, indexSeries, indexName);
            default:
                throw new RuntimeException("Unknown indexSeries.");
        }
    }

    @Override
    public void registerGauge(IndexSeriesImpl impl, GaugeIndex gaugeIndex) {
        String[] indexItems = gaugeIndex.getItems();
        GaugeIndexImpl[] gaugeIndexes = new GaugeIndexImpl[indexItems.length];
        int idx = 0;

        for (String indexItem : indexItems) {
            gaugeIndexes[idx] = (GaugeIndexImpl) getIndex((PerfIndexSeriesImpl)impl, indexItem, IndexType.GAUGE);
            idx++;
        }
        sensorEngine.addGaugeSensor(gaugeIndex, gaugeIndexes);
    }

    public List<Index> getIndexes(PerfIndexSeriesImpl category) {
        Map<String, Index> indexes = this.indexes.get(category.getName());
        if (indexes == null) {
            return Collections.emptyList();
        }
        return new ArrayList<Index>(indexes.values());
    }

    Map<String, PerfIndexSeriesImpl> series = new ConcurrentHashMap<String, PerfIndexSeriesImpl>();

    @Override
    public IndexSeriesImpl createIndexSeriesImpl(String indexSeriesName) {
        PerfIndexSeriesImpl indexSeries = new PerfIndexSeriesImpl(this, indexSeriesName);
        Map<String, IndexType> loadedIndexes = engine.loadIndexes(indexSeriesName);

        for (Map.Entry<String, IndexType> indexDesc : loadedIndexes.entrySet()) {
            getIndex(indexSeries, indexDesc.getKey(), indexDesc.getValue());
        }
        series.put(indexSeriesName, indexSeries);
        return indexSeries;
    }

    public Engine getEngine() {
        return engine;
    }

    public Config getConfig() {
        return config;
    }

    public boolean isCpuSupported(String child) {
        Boolean isAllowCPU = null;
        for (InspectionIndexSeriesType d : config.getIndexSeries()) {
            if (d.getName().equals(child)) {
                isAllowCPU = d.isAllowCPU();
                break;
            }
        }
        return isAllowCPU != null ? isAllowCPU : config.getDefaultIndexSeries().isAllowCPU();
    }

    public List<String> getMapsTo(String childSeries) {
        MapsToType mapsToType = null;
        for (IndexSeriesType indexSeriesType : config.getIndexSeries()) {
            if (indexSeriesType instanceof InspectionIndexSeriesType) {
                InspectionIndexSeriesType inspectionIndexSeriesType = (InspectionIndexSeriesType) indexSeriesType;
                if (inspectionIndexSeriesType.getName().equals(childSeries)) {
                    mapsToType = inspectionIndexSeriesType.getMapsTo();
                    break;
                }
            }
        }
        if (mapsToType == null) {
            mapsToType = config.getDefaultIndexSeries().getMapsTo();
        }
        List<String> result = new ArrayList<String>();
        for (MapsToSeriesType m : mapsToType.getMapsTo()) {
            result.add(m.getName());
        }
        return result;
    }

    public Disruptor<IndexEvent> getDisruptor() {
        return disruptor;
    }

    public PerfIndexSeriesImpl getIndexSeries(String category) {
        return series.get(category);
    }


}
