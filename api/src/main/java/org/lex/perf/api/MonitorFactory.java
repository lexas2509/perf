package org.lex.perf.api;

/**
 */

public abstract class MonitorFactory {

    private static IMonitorFactory monitorFactory;

    public static MonitorCategory getMonitorCategory(String category) {
        return monitorFactory.getMonitorCategory(category);
    }

    public static Index getIndex(MonitorCategory category, String indexName) {
        return monitorFactory.getIndex(category, indexName);
    }

    public static Index getIndex(String categoryName, String indexName) {
        return monitorFactory.getIndex(getMonitorCategory(categoryName), indexName);
    }

    private static void initFactory() {
        try {
            Class cl = MonitorFactory.class.getClassLoader().loadClass("org.lex.perf.engine.MonitorFactoryImpl");
            try {
                monitorFactory = (IMonitorFactory) cl.newInstance();
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

    public static IMonitorFactory getFactory() {
        return monitorFactory;
    }


    public interface IMonitorFactory {
        public MonitorCategory getMonitorCategory(String category);

        public Index getIndex(MonitorCategory category, String indexName);
    }
}
