package org.lex.perf.sensor;


import org.lex.perf.event.MonitoringCategory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 */
public class CPUSensor implements Sensor {
    @Override
    public double getValue() {
        final OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();
        if (operatingSystem.getSystemLoadAverage() >= 0) {
            return operatingSystem.getSystemLoadAverage();
        }
        return -1;
    }

    @Override
    public MonitoringCategory getCategory() {
        return MonitoringCategory.JVM;
    }

    @Override
    public String getItem() {
        return "CPU";
    }
}
