package org.lex.perf.engine;

import org.lex.perf.impl.PerfIndexSeriesImpl;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.slf4j.LoggerFactory;

/**
 */
public class Counter extends Index<CounterTimeSlot> {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Counter.class);

    public Counter(EngineImpl engine, PerfIndexSeriesImpl category, String name) {
        super(engine, category, name);
    }

    protected RrdDef getRrdDef() {
        RrdDef rrdDef = new RrdDef(fileName);
        rrdDef.setStartTime(sampleTime.get() / 1000);
        LOGGER.warn("start " + indexName + ":" + Long.toString(rrdDef.getStartTime()));
        int step = slotDuration / 1000;
        rrdDef.addDatasource("hits", DsType.ABSOLUTE, step, 0, Double.MAX_VALUE);
        rrdDef.addDatasource("total", DsType.ABSOLUTE, step, 0, Double.MAX_VALUE);
        rrdDef.addDatasource("totalcpu", DsType.ABSOLUTE, step, 0, Double.MAX_VALUE);
        if (category.isSupportHistogramm()) {
            for (int i = 0; i < CounterTimeSlot.times.length; i++) {
                rrdDef.addDatasource("hits" + Integer.toString(i), DsType.ABSOLUTE, step, 0, Double.MAX_VALUE);
            }
        }

        for (String child : category.getChildSeries()) {
            rrdDef.addDatasource(child + "_hits", DsType.ABSOLUTE, step, 0, Double.MAX_VALUE);
            rrdDef.addDatasource(child + "_total", DsType.ABSOLUTE, step, 0, Double.MAX_VALUE);
            rrdDef.addDatasource(child + "_totalcpu", DsType.ABSOLUTE, step, 0, Double.MAX_VALUE);
        }

        rrdDef.setStep(1);
        rrdDef.addArchive(ConsolFun.TOTAL, 0.5, step, EngineImpl.HOUR / step); // per slot for hour
        rrdDef.addArchive(ConsolFun.TOTAL, 0.5, EngineImpl.MINUTE, EngineImpl.DAY / EngineImpl.MINUTE); // per minute for day
        rrdDef.addArchive(ConsolFun.TOTAL, 0.5, EngineImpl.HOUR, EngineImpl.WEEK / EngineImpl.DAY); // per hour for week
        return rrdDef;
    }

    @Override
    protected CounterTimeSlot createTimeSlot(long startTime) {
        return new CounterTimeSlot(startTime, startTime + slotDuration, category.isSupportCPU(), category.isSupportHistogramm(), category.getChildSeries());
    }

    @Override
    public String getFileName() {
        return fileName;
    }
}
