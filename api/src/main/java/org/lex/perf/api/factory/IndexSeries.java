package org.lex.perf.api.factory;

/**
 * Created by Алексей on 18.09.2014.
 */
public interface IndexSeries {
    String getName();

    IndexType getIndexType();

    boolean isActive();

    String[] getDurations();

    void addRequest(String indexName, long eventTime, long duration[]);

    void bindContext(String contextName);

    void unbindContext(String contextName);
}
