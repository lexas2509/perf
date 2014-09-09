package org.lex.perf.engine.event;

import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.engine.Engine;

/**
 */

public class MonitoringValue {
    public IndexSeries category;
    public String item;
    public long eventTime;
    public double value;

    public static void sendValueItem(IndexSeries category, String item, long eventTime, double value) {
        MonitoringValue event = new MonitoringValue();
        event.category = category;
        event.item = item;
        event.eventTime = eventTime;
        event.value = value;
        Engine.engine.putSensorValue(event);
    }
}
