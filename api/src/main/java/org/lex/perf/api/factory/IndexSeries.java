package org.lex.perf.api.factory;

/**
 * Created by Алексей on 18.09.2014.
 */
public interface IndexSeries {
    String getName();

    IndexType getIndexType();

    boolean isActive();

    void addRequest(String indexName, long eventTime, long duration);

    void addRequest(String indexName, long eventTime, long duration, long cpuDuration);

    void bindContext(String contextName);

    void unbindContext(String contextName);
}
