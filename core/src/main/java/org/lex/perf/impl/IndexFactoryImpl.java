package org.lex.perf.impl;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.factory.IndexSeriesImpl;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.api.index.GaugeIndex;
import org.lex.perf.api.index.Index;
import org.lex.perf.config.*;
import org.lex.perf.engine.CounterTimeSlot;
import org.lex.perf.engine.EngineImpl;
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

    private final Disruptor<PerfIndexSeriesImpl.IndexEvent> disruptor;


    private SensorEngine sensorEngine;

    private final EngineImpl engine;

    private Map<String, Map<String, Index>> indexes = new ConcurrentHashMap<String, Map<String, Index>>();

    private final Config config;

    public IndexFactoryImpl() {
        String contextPath = "org.lex.perf.config";
        config = JAXBUtil.getObject(contextPath, "defaultConfig.xml");
        engine = new EngineImpl();
        sensorEngine = new SensorEngine(engine);

        // create asynchronous handler (based on disruptor)
        executor = Executors.newSingleThreadExecutor();
        disruptor = new Disruptor<PerfIndexSeriesImpl.IndexEvent>(INDEX_EVENT_FACTORY,
                16384, executor, ProducerType.MULTI, new SleepingWaitStrategy());
        disruptor.handleEventsWith(handler);
        disruptor.start();

    }

    public final EventFactory<PerfIndexSeriesImpl.IndexEvent> INDEX_EVENT_FACTORY = new EventFactory<PerfIndexSeriesImpl.IndexEvent>() {
        @Override
        public PerfIndexSeriesImpl.IndexEvent newInstance() {
            return new PerfIndexSeriesImpl.IndexEvent(MAX_CHILD_SERIES_LENGTH);
        }
    };

    private final EventHandler<PerfIndexSeriesImpl.IndexEvent> handler = new EventHandler<PerfIndexSeriesImpl.IndexEvent>() {
        public void onEvent(final PerfIndexSeriesImpl.IndexEvent event, final long sequence, final boolean endOfBatch) throws Exception {
            CounterTimeSlot ts = event.counter.getTimeSlot(event.requestTime);
            ts.addHit(event.own, event.childsDurations);
        }
    };


    public Index getIndex(PerfIndexSeriesImpl indexSeries, String indexName) {
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

    private Index createIndex(PerfIndexSeriesImpl indexSeries, String indexName) {
        switch (indexSeries.getIndexType()) {
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
            gaugeIndexes[idx] = (GaugeIndexImpl) getIndex((PerfIndexSeriesImpl)impl, indexItem);
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
    public IndexSeriesImpl createIndexSeriesImpl(String indexSeriesName, IndexType indexType) {
        PerfIndexSeriesImpl indexSeries = new PerfIndexSeriesImpl(this, indexSeriesName, indexType);
        List<String> loadedIndexes = engine.loadIndexesFromDisk(getCategoryPrefix(indexSeries), indexSeriesName);
        for (String indexName : loadedIndexes) {
            getIndex(indexSeries, indexName);
        }
        series.put(indexSeriesName, indexSeries);
        return indexSeries;
    }

    public EngineImpl getEngine() {
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

    public Disruptor<PerfIndexSeriesImpl.IndexEvent> getDisruptor() {
        return disruptor;
    }

    public PerfIndexSeriesImpl getIndexSeries(String category) {
        return series.get(category);
    }

    public static String getCategoryPrefix(PerfIndexSeriesImpl category) {
        return category.getIndexType().name().substring(0, 1) + "-" + category.getName();
    }

}
