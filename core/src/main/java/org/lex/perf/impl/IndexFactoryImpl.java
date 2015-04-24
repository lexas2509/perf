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
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 */
public class IndexFactoryImpl implements IndexFactory.IIndexFactory {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(IndexFactoryImpl.class);

    public static final int MAX_CHILD_SERIES_LENGTH = 3;

    public static final String INDEX_FACTORY_CONFIG = "org.lex.perf.config";

    private final java.util.concurrent.Executor executor;

    private final Disruptor<IndexEvent> disruptor;

    private SensorEngine sensorEngine;

    private final Engine engine;

    private Map<String, Map<String, Index>> indexes = new ConcurrentHashMap<String, Map<String, Index>>();

    /**
     * JAXB parsed configuration
     */
    private final Config config;

    /**
     * Number of defined index series plus 1 (corresponds default index)
     */
    private final int seriesCount;

    /**
     * Array to optimize nested context data gathering
     * <p/>
     * The first index - index of child series
     * The second index - index of parent series
     * <p/>
     * The value is -1  means there is no relationship,
     * <p/>
     * another value is idx in childSeries array of parent index corresponds that child series
     */
    private final int[][] nestedChildIndexes;

    /**
     *
     */
    private final IndexSeriesType[] indexSeriesTypes;

    private final Map<String, Integer> indexMap = new HashMap<String, Integer>();


    public IndexFactoryImpl() {
        this(JAXBUtil.getObject(INDEX_FACTORY_CONFIG, "defaultConfig.xml", Config.class));
    }

    IndexFactoryImpl(Config config) {
        this.config = config;
        engine = EngineFactory.getEngine();
        sensorEngine = new SensorEngine(engine);

        // create asynchronous handler (based on disruptor)
        executor = Executors.newSingleThreadExecutor();
        disruptor = new Disruptor<IndexEvent>(INDEX_EVENT_FACTORY,
                16384, executor, ProducerType.MULTI, new SleepingWaitStrategy());
        disruptor.handleEventsWith(engine.getHandler());
        disruptor.start();

        seriesCount = config.getIndexSeries().size() + 1;

        int idx = 0;
        indexSeriesTypes = new IndexSeriesType[seriesCount];
        for (IndexSeriesType indexSeriesType : config.getIndexSeries()) {
            indexSeriesTypes[idx] = indexSeriesType;
            indexMap.put(indexSeriesType.getName(), idx);
            idx++;
        }
        IndexSeriesType defaultIndexSeries = config.getDefaultIndexSeries();
        indexSeriesTypes[idx] = defaultIndexSeries;
        indexMap.put(defaultIndexSeries.getName(), idx);


        nestedChildIndexes = new int[seriesCount][seriesCount];
        initNestedChildIndexes();
    }

    private void initNestedChildIndexes() {
        for (int parentIndex = 0; parentIndex < seriesCount; parentIndex++) {
            IndexSeriesType parent = indexSeriesTypes[parentIndex];
            for (int childIndex = 0; childIndex < seriesCount; childIndex++) {
                IndexSeriesType child = indexSeriesTypes[childIndex];
                Set<String> scannedIndexes = new HashSet<String>();
                nestedChildIndexes[childIndex][parentIndex] = findNestedParent(child, parent, scannedIndexes);
            }
        }
    }

    private int findNestedParent(IndexSeriesType child, IndexSeriesType parent, Set<String> scannedIndexes) {
        ChildSeriesType childSeries = parent.getChildSeries();
        if (childSeries == null) {
            return -1;
        }
        List<ChildIndexSeriesType> childIndexSeries = childSeries.getChildIndexSeries();
        for (int i = 0; i < childIndexSeries.size(); i++) {
            ChildIndexSeriesType childIndexSeriesType = childIndexSeries.get(i);
            if (child.getName().equals(childIndexSeriesType.getName())) {
                return i;
            }
        }
        MapsToType mapsTo = child.getMapsTo();
        if (mapsTo == null) {
            return -1;
        }
        List<MapsToSeriesType> mapsToType = mapsTo.getMapsTo();
        if (mapsToType == null || mapsToType.size() == 0) {
            return -1;
        }
        for (MapsToSeriesType mapsToSeriesType : mapsToType) {
            String mapsToIndexSeriesName = mapsToSeriesType.getName();
            if (scannedIndexes.contains(mapsToIndexSeriesName)) {
                continue;
            }

            Integer mapsToChildIdx = this.indexMap.get(mapsToIndexSeriesName);
            if (mapsToChildIdx == null) {
                LOGGER.error("Unknown indexSeriesName [{}] in mapsTo for indexSeries [{}]", mapsToIndexSeriesName, child.getName());
                continue;
            }
            IndexSeriesType mapsToChild = indexSeriesTypes[mapsToChildIdx];
            HashSet<String> nextScannedIndexes = new HashSet<String>(scannedIndexes);
            nextScannedIndexes.add(child.getName());
            int res = findNestedParent(mapsToChild, parent, nextScannedIndexes);
            if (res > -1) {
                return res;
            }
        }
        return -1;
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
            gaugeIndexes[idx] = (GaugeIndexImpl) getIndex((PerfIndexSeriesImpl) impl, indexItem, IndexType.GAUGE);
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
        for (IndexSeriesType d : config.getIndexSeries()) {
            if (d.getName().equals(child)) {
                isAllowCPU = d.isAllowCPU();
                break;
            }
        }
        return isAllowCPU != null ? isAllowCPU : config.getDefaultIndexSeries().isAllowCPU();
    }

    public Disruptor<IndexEvent> getDisruptor() {
        return disruptor;
    }

    public PerfIndexSeriesImpl getIndexSeries(String category) {
        return series.get(category);
    }

    public int[][] getNestedChildIndexes() {
        return nestedChildIndexes;
    }

    public int getSeriesCount() {
        return seriesCount;
    }

    /**
     * return the indexSeries's index in list. (default indexSeries is last, others has idx as they are defined in xml)
     *
     * @param indexSeriesName
     * @return index of IndexSeries
     */
    public Integer getIndexSeriesIdx(String indexSeriesName) {
        return indexMap.get(indexSeriesName);
    }
}
