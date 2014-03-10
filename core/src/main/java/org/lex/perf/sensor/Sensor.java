package org.lex.perf.sensor;

import org.lex.perf.event.MonitoringCategory;

import java.util.Map;

/**
 */

public interface Sensor {
    Map<String, Double> getValues();

    MonitoringCategory getCategory();

    String[] getItems();
}
