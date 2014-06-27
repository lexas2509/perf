package org.lex.perf.api;

/**
 */
public interface MonitorCategory {

    public String getName();

    public CategoryType getCategoryType();

    public boolean isActive();

    void addRequest(String indexName, long duration);

    void bindContext(String contextName);
}
