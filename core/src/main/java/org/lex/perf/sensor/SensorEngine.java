package org.lex.perf.sensor;

import org.lex.perf.api.index.GaugeIndex;
import org.lex.perf.engine.Engine;
import org.lex.perf.impl.GaugeIndexImpl;
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

    private static class GaugeIndexPair {
        GaugeIndex gaugeIndex;
        GaugeIndexImpl[] gaugeIndexes;

        public GaugeIndexPair(GaugeIndex gaugeIndex, GaugeIndexImpl[] gaugeImpls) {
            this.gaugeIndex = gaugeIndex;
            this.gaugeIndexes = gaugeImpls;
        }
    }

    private final List<GaugeIndexPair> gauges = new ArrayList<GaugeIndexPair>();

    private Timer sensorTime = new Timer();

    public SensorEngine(final Engine engine) {
        sensorTime.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (GaugeIndexPair sensor : gauges) {
                        BigDecimal[] sensorData = sensor.gaugeIndex.getValues();
                        String[] sensorItems = sensor.gaugeIndex.getItems();
                        long eventTime = System.currentTimeMillis();

                        for (int i = 0; i < sensorItems.length; i++) {
                            GaugeIndexImpl indexImpl = sensor.gaugeIndexes[i];
                            indexImpl.putSensorValue(eventTime, sensorData[i]);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("", e);
                }

            }
        }, SECOND, 10 * SECOND);
    }

    public void addGaugeSensor(GaugeIndex gaugeIndex, GaugeIndexImpl[] gaugeImpls) {
        gauges.add(new GaugeIndexPair(gaugeIndex, gaugeImpls));
    }

}
