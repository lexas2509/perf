package org.lex.perf.engine;

import org.lex.perf.event.MonitoringCategory;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Util;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 */
public class Counter extends Index<CounterTimeSlot> {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Counter.class);

    public Counter(MonitoringCategory category, String name) {
        super(category, name);
        try {
            RrdDef rrdDef = new RrdDef("e:/mondata/" + fileName + ".rrd");
            rrdDef.setStartTime(sampleTime.get() / 1000);
            LOGGER.warn("start " + itemName + ":" + Long.toString(rrdDef.getStartTime()));
            int step = slotDuration / 1000;
            rrdDef.addDatasource("hits", DsType.ABSOLUTE, step, 0, Double.MAX_VALUE);
            rrdDef.addDatasource("total", DsType.ABSOLUTE, step, 0, Double.MAX_VALUE);

            rrdDef.setStep(1);
            rrdDef.addArchive(ConsolFun.TOTAL, 0.5, step, Engine.HOUR / step); // per slot for hour
            rrdDef.addArchive(ConsolFun.TOTAL, 0.5, Engine.MINUTE, Engine.DAY / Engine.MINUTE); // per minute for day
            rrdDef.addArchive(ConsolFun.TOTAL, 0.5, Engine.HOUR, Engine.WEEK / Engine.DAY); // per hour for day
            rrdDb = new RrdDb(rrdDef);
            rrdDb.close();
            rrdDb = new RrdDb(rrdDef);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
