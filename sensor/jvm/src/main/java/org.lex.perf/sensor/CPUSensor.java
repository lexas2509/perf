package org.lex.perf.sensor;


import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.api.index.GaugeIndex;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.math.BigDecimal;

/**
 */
public class CPUSensor implements GaugeIndex {

    private boolean isActive = true;

    @Override
    public BigDecimal[] getValues() {
        BigDecimal[] result = new BigDecimal[1];
        final OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();
        if (operatingSystem.getSystemLoadAverage() >= 0) {
            result[0] = new BigDecimal(operatingSystem.getSystemLoadAverage());
        } else {
            result[0] = BigDecimal.ZERO;
        }
        return result;
    }

    @Override
    public IndexSeries getIndexSeries() {
        return org.lex.perf.sensor.JVMGauges.JVM;
    }

    @Override
    public String[] getItems() {
        return new String[]{"CPU"};
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public IndexType getIndexType() {
        return IndexType.GAUGE;
    }
}
