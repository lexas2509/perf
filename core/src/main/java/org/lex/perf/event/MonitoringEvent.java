package org.lex.perf.event;

/**
 */
public class MonitoringEvent {
    public MonitoringCategory category;
    public String item;
    public long time;

    public static void sendItem(MonitoringCategory category, String contextPath, long time) {
        MonitoringEvent event = new MonitoringEvent();
        event.category = category;
        event.item = contextPath;
        event.time = time;

    }
}
