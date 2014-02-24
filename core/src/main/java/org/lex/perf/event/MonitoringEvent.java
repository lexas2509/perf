package org.lex.perf.event;

import org.lex.perf.engine.Engine;

/**
 */
public class MonitoringEvent {
    public MonitoringCategory category;
    public String item;
    public long eventTime;
    public long duration;

    public static void sendItem(MonitoringCategory category, String item, long eventTime, long duration) {
        MonitoringEvent event = new MonitoringEvent();
        event.category = category;
        event.item = item;
        event.eventTime = eventTime;
        event.duration = duration;
        Engine.engine.putEvent(event);
    }
}
