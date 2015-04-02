package org.lex.perf.engine;

import com.lmax.disruptor.EventHandler;
import org.lex.perf.api.factory.IndexType;

import java.util.Map;

/**
 */
public interface Engine {

    public EventHandler<IndexEvent> getHandler();

    EngineIndex getCounter(String indexName, String indexSeriesName, IndexType indexType, boolean supportCPU, boolean supportHistogramm, String[] childSeries);

    EngineIndex getGauge(String indexName, String name);

    Map<String,IndexType> loadIndexes(String indexSeriesName);
}
