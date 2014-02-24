package org.lex.perf.engine;

import org.lex.perf.event.MonitoringCategory;
import org.lex.perf.event.MonitoringEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public class Engine {

    private final static Logger LOGGER = LoggerFactory.getLogger(Engine.class);
    public static Engine engine;

    private Map<String, Map<String, Counter>> counters = new ConcurrentHashMap<String, Map<String, Counter>>();

    private Timer timer = new Timer();

    static {
        engine = new Engine();
    }

    public Engine() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    s.doCleanup(System.currentTimeMillis());
                } catch (Throwable t) {
                    LOGGER.error("error ", t);
                }
            }
        }, 10000, 10 * 1000);
    }

    public void putEvent(MonitoringEvent event) {
        Counter counter = engine.getCounter(event.category, event.item);
        CounterTimeSlot timeSlot = counter.getTimeSlot(event.eventTime);
        timeSlot.addHit(event.duration);
    }

    private final Counter s = new Counter();

    private Counter getCounter(MonitoringCategory category, String item) {
        return s;
    }
}
