package org.lex.perf.sensor;


import org.lex.perf.api.MonitorCategory;
import org.lex.perf.common.StandardCategory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class CPUSensor implements Sensor {
    @Override
    public Map<String, Double> getValues() {
        Map<String, Double> map = new HashMap<String, Double>();
        final OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();
        if (operatingSystem.getSystemLoadAverage() >= 0) {
            map.put("CPU", operatingSystem.getSystemLoadAverage());
        } else {
            map.put("CPU", new Double(-1));
        }
        return map;
    }

    @Override
    public MonitorCategory getCategory() {
        return StandardCategory.JVM;
    }

    @Override
    public String[] getItems() {
        return new String[]{"CPU"};
    }
}
