package org.lex.perf.api.factory;

import org.lex.perf.api.index.GaugeIndex;
import org.lex.perf.api.index.Index;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 */

public abstract class IndexFactory {

    private static IIndexFactory indexFactory;

    public static Index getIndex(IndexSeries indexSeries, String indexName) {
        return indexFactory.getIndex(indexSeries, indexName);
    }

    public static Index getIndex(String indexSeries, String indexName) {
        return indexFactory.getIndex(getIndexSeries(indexSeries), indexName);
    }

    private static void initFactory() {
        try {
            Class cl = IndexFactory.class.getClassLoader().loadClass("org.lex.perf.impl.IndexFactoryImpl");
            try {
                indexFactory = (IIndexFactory) cl.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        initFactory();
    }

    public static IIndexFactory getFactory() {
        return indexFactory;
    }


    public interface IIndexFactory {
        public Index getIndex(IndexSeries indexSeries, String indexName);

        public void registerGauge(GaugeIndex gaugeIndex);
    }

    private final static Map<String, IndexSeries> INDEX_SERIES = new ConcurrentHashMap<String, IndexSeries>();

    public static IndexSeries getIndexSeries(String indexSeries) {
        return INDEX_SERIES.get(indexSeries);
    }

    public static IndexSeries registerIndexSeries(IndexSeries indexSeries) {
        INDEX_SERIES.put(indexSeries.getName(), indexSeries);
        return indexSeries;
    }
}
