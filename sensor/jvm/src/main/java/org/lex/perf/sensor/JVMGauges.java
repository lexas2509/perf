package org.lex.perf.sensor;

import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexSeries;

/**
 * Created by Алексей on 08.09.2014.
 */
public class JVMGauges {

    /**
     * Серия индексов, отображащающих одномоментные показатели JVM (текущее кол-во потоков, загруженность CPU и т.п.)
     */
    public static final IndexSeries JVM = IndexFactory.registerIndexSeries("JVM");

    static {
        IndexFactory.registerGauge(JVM, new org.lex.perf.sensor.CPUSensor());
        IndexFactory.registerGauge(JVM, new org.lex.perf.sensor.HeapSensor());
    }

    public static void init() {

    }
}
