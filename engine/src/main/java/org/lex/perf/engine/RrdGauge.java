package org.lex.perf.engine;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.slf4j.LoggerFactory;

/**
 */
public class RrdGauge extends RrdIndex<GaugeTimeSlot> {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RrdGauge.class);

    public RrdGauge(EngineImpl engine, String name, String fileName) {
        super(engine, name, fileName);
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
