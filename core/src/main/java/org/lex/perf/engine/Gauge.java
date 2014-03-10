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
public class Gauge extends Index<GaugeTimeSlot> {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Gauge.class);

    public Gauge(MonitoringCategory category, String name) {
        super(category, name);
        try {
            RrdDef rrdDef = new RrdDef("e:/mondata/" + fileName + ".rrd");
            rrdDef.setStartTime(sampleTime.get() / 1000);
            LOGGER.warn( "start " + itemName + ":" + Long.toString(rrdDef.getStartTime()));
            rrdDef.addDatasource("value", DsType.GAUGE, slotDuration / 1000, -1, Double.MAX_VALUE);
            rrdDef.setStep(slotDuration / 1000);
            rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 60 * 12);
            rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 12, 24 * 60 );
            rrdDef.addArchive(ConsolFun.MAX, 0.5, 1, 60 * 12);
            rrdDef.addArchive(ConsolFun.MIN, 0.5, 1, 60 * 12);
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
    protected GaugeTimeSlot createTimeSlot(long startTime) {
        return new GaugeTimeSlot(startTime, startTime + slotDuration);
    }

    @Override
    public String getFileName() {
        return fileName;
    }
}
