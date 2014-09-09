package org.lex.perf.impl;

import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.index.GaugeIndex;
import org.lex.perf.api.index.Index;
import org.lex.perf.sensor.SensorEngine;

/**
 */
public class IndexFactoryImpl implements IndexFactory.IIndexFactory {


    private SensorEngine sensorEngine;

    public IndexFactoryImpl() {
        sensorEngine = new SensorEngine();
    }

    @Override
    public Index getIndex(IndexSeries category, String indexName) {
        return null;
    }

    @Override
    public void registerGauge(GaugeIndex gaugeIndex) {
        sensorEngine.addGaugeSensor(gaugeIndex);
    }
}
