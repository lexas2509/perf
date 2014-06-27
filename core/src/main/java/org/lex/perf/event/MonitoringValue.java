package org.lex.perf.event;

import org.lex.perf.api.MonitorCategory;
import org.lex.perf.engine.Engine;

/**
 */

public class MonitoringValue {
    public MonitorCategory category;
    public String item;
    public long eventTime;
    public double value;

    public static void sendValueItem(MonitorCategory category, String item, long eventTime, double value) {
        MonitoringValue event = new MonitoringValue();
        event.category = category;
        event.item = item;
        event.eventTime = eventTime;
        event.value = value;
        Engine.engine.putSensorValue(event);
    }
}
