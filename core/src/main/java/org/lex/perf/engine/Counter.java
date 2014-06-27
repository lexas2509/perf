package org.lex.perf.engine;

import org.lex.perf.api.MonitorCategory;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.slf4j.LoggerFactory;

/**
 */
public class Counter extends Index<CounterTimeSlot> {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Counter.class);

    public Counter(Engine engine, MonitorCategory category, String name) {
        super(engine, category, name);
    }

    protected RrdDef getRrdDef() {
        RrdDef rrdDef = new RrdDef(fileName);
        rrdDef.setStartTime(sampleTime.get() / 1000);
        LOGGER.warn("start " + indexName + ":" + Long.toString(rrdDef.getStartTime()));
        int step = slotDuration / 1000;
        rrdDef.addDatasource("hits", DsType.ABSOLUTE, step, 0, Double.MAX_VALUE);
        rrdDef.addDatasource("total", DsType.ABSOLUTE, step, 0, Double.MAX_VALUE);
        for (int i = 0; i < CounterTimeSlot.times.length + 1; i++) {
            rrdDef.addDatasource("hits" + Integer.toString(i), DsType.ABSOLUTE, step, 0, Double.MAX_VALUE);
        }

        rrdDef.setStep(1);
        rrdDef.addArchive(ConsolFun.TOTAL, 0.5, step, Engine.HOUR / step); // per slot for hour
        rrdDef.addArchive(ConsolFun.TOTAL, 0.5, Engine.MINUTE, Engine.DAY / Engine.MINUTE); // per minute for day
        rrdDef.addArchive(ConsolFun.TOTAL, 0.5, Engine.HOUR, Engine.WEEK / Engine.DAY); // per hour for week
        return rrdDef;
    }

    @Override
    protected CounterTimeSlot createTimeSlot(long startTime) {
        return new CounterTimeSlot(startTime, startTime + slotDuration);
    }

    @Override
    public String getFileName() {
        return fileName;
    }
}
