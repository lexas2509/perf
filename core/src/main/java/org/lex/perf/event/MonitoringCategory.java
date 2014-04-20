package org.lex.perf.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public class MonitoringCategory {

    private final static Map<String, MonitoringCategory> categorites = new ConcurrentHashMap<String, MonitoringCategory>();

    public static final MonitoringCategory JVM = new MonitoringCategory("JVM", CategoryType.GAUGE);

    public static final MonitoringCategory HTTP = new MonitoringCategory("HTTP", CategoryType.COUNTER);

    public static final MonitoringCategory SQL = new MonitoringCategory("SQL", CategoryType.COUNTER);


    private final String name;

    private final CategoryType categoryType;

    public MonitoringCategory(String name, CategoryType categoryType) {
        this.name = name;
        this.categoryType = categoryType;
        categorites.put(name, this);
    }

    public String getName() {
        return name;
    }

    public CategoryType getCategoryType() {
        return categoryType;
    }

    public static MonitoringCategory get(String category) {
        return categorites.get(category);
    }


}
