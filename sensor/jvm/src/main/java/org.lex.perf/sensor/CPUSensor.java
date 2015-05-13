package org.lex.perf.sensor;


import org.lex.perf.api.index.GaugeSensorImpl;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.math.BigDecimal;

/**
 */
public class CPUSensor extends GaugeSensorImpl {

    @Override
    public BigDecimal[] getValues() {
        BigDecimal[] result = new BigDecimal[1];
        final OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();
        if (operatingSystem.getSystemLoadAverage() >= 0) {
            result[0] = BigDecimal.valueOf(operatingSystem.getSystemLoadAverage());
        } else {
            result[0] = BigDecimal.ZERO;
        }
        return result;
    }

    @Override
    public String[] getItems() {
        return new String[]{"CPU"};
    }

    @Override
    public String getName() {
        return "CPU";
    }
}
