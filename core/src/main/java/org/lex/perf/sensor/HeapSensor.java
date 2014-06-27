package org.lex.perf.sensor;


import org.lex.perf.api.MonitorCategory;
import org.lex.perf.common.StandardCategory;

import java.lang.management.*;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class HeapSensor implements Sensor {

    @Override
    public MonitorCategory getCategory() {
        return StandardCategory.JVM;
    }

    public Map<String, Double> getValues() {
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

        Map<String, Double> result = new HashMap<String, Double>();
        result.put("Heap", usedMemory);
        result.put("MaxHeap", maxMemory);
        result.put("PermGen", usedPermGen);

        return result;
    }

    @Override
    public String[] getItems() {
        return new String[]{"Heap", "MaxHeap", "PermGen"};
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
