package org.lex.perf.engine;

import org.lex.perf.event.MonitoringCategory;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Util;

import java.io.IOException;

/**
 */
public class Gauge extends Index<GaugeTimeSlot> {

    private final String fileName;

    public Gauge(MonitoringCategory category, String name) {
        super(category, name);
        try {
            fileName = "e:/mondata/" + counterName + ".rrd";
            RrdDef rrdDef = new RrdDef(fileName);
            rrdDef.setStartTime(Util.getTime() - 1);
            rrdDef.addDatasource("value", DsType.ABSOLUTE, 1, 0, Double.NaN);
            rrdDef.setStep(10);
            rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 7 * 24 * 60);
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
