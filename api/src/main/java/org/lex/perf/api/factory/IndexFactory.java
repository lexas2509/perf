package org.lex.perf.api.factory;

import org.lex.perf.api.index.GaugeIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 */

public abstract class IndexFactory {

    private final static Logger LOGGER = LoggerFactory.getLogger(IndexFactory.class);

    private static IIndexFactory indexFactory;

    private static void initFactory() {
        Class cl;
        try {
            cl = IndexFactory.class.getClassLoader().loadClass("org.lex.perf.impl.IndexFactoryImpl");
            indexFactory = (IIndexFactory) cl.newInstance();
        } catch (Exception e) {
            LOGGER.error("Can't instantiate IndexFactory", e);
            indexFactory = new NopIndexFactory();
        }
    }

    static {
        initFactory();
    }

    public static IIndexFactory getFactory() {
        return indexFactory;
    }


    public interface IIndexFactory {
        public void registerGauge(IndexSeriesImpl impl, GaugeIndex gaugeIndex);

        IndexSeriesImpl createIndexSeriesImpl(String indexSeriesName);
    }

    private final static Map<String, IndexSeries> INDEX_SERIES = new ConcurrentHashMap<String, IndexSeries>();

    public static synchronized IndexSeries registerIndexSeries(String indexSeriesName) {
        if (INDEX_SERIES.containsKey(indexSeriesName)) {
            return INDEX_SERIES.get(indexSeriesName);
        } else {
            IndexSeriesImpl indexSeriesImpl = indexFactory.createIndexSeriesImpl(indexSeriesName);
            IndexSeries indexSeries = new IndexSeries(indexSeriesName);
            indexSeries.configure(indexSeriesImpl, true);
            INDEX_SERIES.put(indexSeriesName, indexSeries);
            return indexSeries;
        }
    }

    public static void registerGauge(IndexSeries indexSeries, GaugeIndex gaugeIndex) {
        IndexSeriesImpl indexSeriesImpl = indexSeries.getImpl();
        getFactory().registerGauge(indexSeriesImpl, gaugeIndex);
        gaugeIndex.setIndexSeries(indexSeries);
    }

}
