package org.lex.perf.engine;

import org.lex.perf.api.Index;
import org.lex.perf.api.MonitorCategory;
import org.lex.perf.api.MonitorFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public class MonitorFactoryImpl implements MonitorFactory.IMonitorFactory {

    private final static Map<String, MonitorCategory> categories = new ConcurrentHashMap<String, MonitorCategory>();

    public MonitorFactoryImpl() {
    }

    public MonitorCategory getMonitorCategory(String category) {
        return categories.get(category);
    }

    public void registerMonitorCategory(String category, MonitorCategory monitorCategory) {
        categories.put(category, monitorCategory);
    }

    @Override
    public Index getIndex(MonitorCategory category, String indexName) {
        return null;
    }
}
