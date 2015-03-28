package org.lex.perf.sensor;


import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.api.index.GaugeIndex;
import org.lex.perf.api.index.GaugeSensorImpl;

import java.lang.management.*;
import java.math.BigDecimal;

/**
 */
public class HeapSensor extends GaugeSensorImpl {

    public static final String[] HEAP_SENSORS = new String[]{"Heap", "MaxHeap", "PermGen"};

    @Override
    public BigDecimal[] getValues() {
        double usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        double maxMemory = Runtime.getRuntime().maxMemory();

        double usedPermGen;
        double maxPermGen;
        final MemoryPoolMXBean permGenMemoryPool = getPermGenMemoryPool();
        if (permGenMemoryPool != null) {
            final MemoryUsage usage = permGenMemoryPool.getUsage();
            usedPermGen = usage.getUsed();
            maxPermGen = usage.getMax();
        } else {
            usedPermGen = -1;
            maxPermGen = -1;
        }
        double usedNonHeapMemory = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
        double loadedClassesCount = ManagementFactory.getClassLoadingMXBean().getLoadedClassCount();
        double garbageCollectionTimeMillis = buildGarbageCollectionTimeMillis();

        double usedPhysicalMemorySize;
        double usedSwapSpaceSize;

        final OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();
        if (isSunOsMBean(operatingSystem)) {
            final com.sun.management.OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean) operatingSystem;
            usedPhysicalMemorySize = osBean.getTotalPhysicalMemorySize()
                    - osBean.getFreePhysicalMemorySize();
            usedSwapSpaceSize = osBean.getTotalSwapSpaceSize() - osBean.getFreeSwapSpaceSize();
        } else {
            usedPhysicalMemorySize = -1;
            usedSwapSpaceSize = -1;
        }

        BigDecimal[] result = new BigDecimal[3];
        result[0] = new BigDecimal(usedMemory);
        result[1] = new BigDecimal(maxMemory);
        result[2] = new BigDecimal(usedPermGen);
        return result;
    }

    @Override
    public String[] getItems() {
        return HEAP_SENSORS;
    }

    private static MemoryPoolMXBean getPermGenMemoryPool() {
        for (final MemoryPoolMXBean memoryPool : ManagementFactory.getMemoryPoolMXBeans()) {
            // name est "Perm Gen" ou "PS Perm Gen" (32 vs 64 bits ?)
            if (memoryPool.getName().endsWith("Perm Gen")) {
                return memoryPool;
            }
        }
        return null;
    }

    private static long buildGarbageCollectionTimeMillis() {
        long garbageCollectionTime = 0;
        for (final GarbageCollectorMXBean garbageCollector : ManagementFactory
                .getGarbageCollectorMXBeans()) {
            garbageCollectionTime += garbageCollector.getCollectionTime();
        }
        return garbageCollectionTime;
    }

    private static boolean isSunOsMBean(OperatingSystemMXBean operatingSystem) {
        // on ne teste pas operatingSystem instanceof com.sun.management.OperatingSystemMXBean
        // car le package com.sun n'existe à priori pas sur une jvm tierce
        final String className = operatingSystem.getClass().getName();
        return "com.sun.management.OperatingSystem".equals(className)
                || "com.sun.management.UnixOperatingSystem".equals(className);
    }

}
