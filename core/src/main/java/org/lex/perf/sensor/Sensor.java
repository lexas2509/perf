package org.lex.perf.sensor;

import org.lex.perf.api.MonitorCategory;

import java.util.Map;

/**
 */

public interface Sensor {
    Map<String, Double> getValues();

    MonitorCategory getCategory();

    String[] getItems();
}
