package org.lex.perf.engine;

import org.lex.perf.api.factory.IndexSeries;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.slf4j.LoggerFactory;

/**
 */
public class Gauge extends Index<GaugeTimeSlot> {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Gauge.class);

    public Gauge(EngineImpl engine, IndexSeries category, String name) {
        super(engine, category, name);
    }

    protected RrdDef getRrdDef() {
        RrdDef rrdDef = new RrdDef(fileName);
        rrdDef.setStartTime(sampleTime.get() / 1000);
        LOGGER.warn("start " + indexName + ":" + Long.toString(rrdDef.getStartTime()));
        rrdDef.addDatasource("value", DsType.GAUGE, slotDuration / 1000, -1, Double.MAX_VALUE);
        rrdDef.setStep(slotDuration / 1000);
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 60 * 12);
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 12, 24 * 60);
        rrdDef.addArchive(ConsolFun.MAX, 0.5, 1, 60 * 12);
        rrdDef.addArchive(ConsolFun.MIN, 0.5, 1, 60 * 12);
        return rrdDef;
    }

    @Override
    protected GaugeTimeSlot createTimeSlot(long startTime) {
        return new GaugeTimeSlot(startTime, startTime + slotDuration);
    }

    @Override
    public String getFileName() {
        return fileName;
    }
}
