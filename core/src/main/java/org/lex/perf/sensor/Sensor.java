package org.lex.perf.sensor;

import org.lex.perf.event.MonitoringCategory;

/**
 * Created with IntelliJ IDEA.
 * User: lexas
 * Date: 26.02.14
 * Time: 9:39
 * To change this template use File | Settings | File Templates.
 */
public interface Sensor {
    double getValue();

    MonitoringCategory getCategory();

    String getItem();
}
