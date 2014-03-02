package org.lex.perf.sensor;

import org.lex.perf.event.MonitoringCategory;
import org.lex.perf.event.MonitoringEvent;
import org.lex.perf.event.MonitoringValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 */
public class SensorEngine {
    private final List<Sensor> sensors = new ArrayList<Sensor>();

    private Timer sensorTime = new Timer();

    static {
        new SensorEngine();
    }

    public SensorEngine() {
        sensors.add(new CPUSensor());

        sensorTime.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Sensor sensor : sensors) {
                    double value = sensor.getValue();
                    MonitoringValue.sendValueItem(sensor.getCategory(), sensor.getItem(), System.currentTimeMillis(), value);
                }
            }
        }, 1000, 60000);
    }

}
