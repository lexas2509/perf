package org.lex.perf.engine.event;

import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.engine.Engine;

/**
 */
public class MonitoringEvent {
    public IndexSeries category;
    public String item;
    public long eventTime;
    public long duration;

    public static void sendDurationItem(IndexSeries category, String item, long eventTime, long duration) {
        MonitoringEvent event = new MonitoringEvent();
        event.category = category;
        event.item = item;
        event.eventTime = eventTime;
        event.duration = duration;
        Engine.engine.putEvent(event);
    }
}
