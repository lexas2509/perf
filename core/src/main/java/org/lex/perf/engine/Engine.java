package org.lex.perf.engine;

import org.lex.perf.event.MonitoringCategory;
import org.lex.perf.event.MonitoringEvent;
import org.lex.perf.event.MonitoringValue;
import org.lex.perf.sensor.SensorEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public class Engine {

    private final static Logger LOGGER = LoggerFactory.getLogger(Engine.class);

    public static Engine engine;

    public static final int SAMPLE_DURATION = 5 * 1000; // 5 sec in ms

    public static final int MINUTE = 60;

    public static final int HOUR = 60 * MINUTE;

    public static final int DAY = 24 * HOUR;

    public static final int WEEK = 7 * DAY;

    private Timer timer = new Timer();

    static {
        engine = new Engine();
        new SensorEngine();
    }


    public Engine() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    long currentTime = System.currentTimeMillis();
                    for (Map<String, Index> entry : indexes.values()) {
                        for (Index s : entry.values()) {
                            s.doSample(currentTime);
                        }
                    }
                } catch (Throwable t) {
                    LOGGER.error("error ", t);
                }
            }
        }, SAMPLE_DURATION - 1, SAMPLE_DURATION); // 10 sec
    }

    public void putEvent(MonitoringEvent event) {
        Counter counter = (Counter) (engine.getIndex(event.category, event.item));
        CounterTimeSlot timeSlot = counter.getTimeSlot(event.eventTime);
        timeSlot.addHit(event.duration / 1000 / 1000);
    }

    private final Map<MonitoringCategory, Map<String, Index>> indexes = new ConcurrentHashMap<MonitoringCategory, Map<String, Index>>();

    public Index getIndex(MonitoringCategory category, String item) {
        Map<String, Index> categoryIndexes = indexes.get(category);
        if (categoryIndexes == null) {
            categoryIndexes = new ConcurrentHashMap<String, Index>();
            indexes.put(category, categoryIndexes);
        }
        Index result = categoryIndexes.get(item);
        if (result == null) {
            switch (category.getCategoryType()) {
                case GAUGE:
                    result = new Gauge(category, item);
                    break;
                case COUNTER:
                    result = new Counter(category, item);
                    break;
                default:
                    break;
            }
            categoryIndexes.put(item, result);
        }
        return result;
    }


    public void putSensorValue(MonitoringValue event) {
        Gauge gauge = (Gauge) engine.getIndex(event.category, event.item);
        GaugeTimeSlot timeSlot = gauge.getTimeSlot(event.eventTime);
        timeSlot.setValue(event.value);
    }

    public List<Index> getIndexes(MonitoringCategory category) {
        return new ArrayList<Index>(indexes.get(category).values());
    }
}
