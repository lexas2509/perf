package org.lex.perf.sensor;

import org.lex.perf.event.MonitoringValue;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 */
public class SensorEngine {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SensorEngine.class);

    private final List<Sensor> sensors = new ArrayList<Sensor>();

    private Timer sensorTime = new Timer();

    public SensorEngine() {
        sensors.add(new CPUSensor());
        sensors.add(new HeapSensor());

        sensorTime.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                for (Sensor sensor : sensors) {
                    Map<String, Double> sensorData = sensor.getValues();
                    String[] sensorItems = sensor.getItems();
                    long eventTime = System.currentTimeMillis();

                    for (String sensorItem : sensorItems) {
                        MonitoringValue.sendValueItem(sensor.getCategory(), sensorItem, eventTime, sensorData.get(sensorItem));
                    }
                }
                } catch (Exception e) {
                    LOGGER.error("", e);
                }

            }
        }, 1000, 10000);
    }

}
