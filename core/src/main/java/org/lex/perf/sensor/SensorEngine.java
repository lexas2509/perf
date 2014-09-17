package org.lex.perf.sensor;

import org.lex.perf.api.index.GaugeIndex;
import org.lex.perf.engine.EngineImpl;
import org.lex.perf.engine.event.MonitoringValue;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 */
public class SensorEngine {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SensorEngine.class);
    public static final int SECOND = 1000;

    private final List<GaugeIndex> gauges = new ArrayList<GaugeIndex>();

    private Timer sensorTime = new Timer();

    public SensorEngine(final EngineImpl engine) {
        sensorTime.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (GaugeIndex sensor : gauges) {
                        BigDecimal[] sensorData = sensor.getValues();
                        String[] sensorItems = sensor.getItems();
                        long eventTime = System.currentTimeMillis();

                        for (int i = 0; i < sensorItems.length; i++) {
                            MonitoringValue event = new MonitoringValue();
                            event.category = sensor.getIndexSeries();
                            event.item = sensorItems[i];
                            event.eventTime = eventTime;
                            event.value = sensorData[i].doubleValue();
                            engine.putSensorValue(event);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("", e);
                }

            }
        }, SECOND, 10 * SECOND);
    }

    public void addGaugeSensor(GaugeIndex gaugeIndex) {
        gauges.add(gaugeIndex);
    }

}
