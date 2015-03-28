package org.lex.perf.impl;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.lex.perf.api.factory.IndexSeriesImpl;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.config.ChildIndexSeriesType;
import org.lex.perf.config.ChildSeriesType;
import org.lex.perf.config.Config;
import org.lex.perf.config.InspectionIndexSeriesType;
import org.lex.perf.engine.Counter;
import org.lex.perf.engine.CounterTimeSlot;

import java.util.ArrayList;
import java.util.concurrent.Executors;

/**
 * Created by Алексей on 18.09.2014.
 */
public class PerfIndexSeriesImpl extends IndexSeriesImpl {


    private String[] childSeries;
    private final Boolean supportCPU;
    private boolean supportHistogramm;

    private IndexFactoryImpl indexFactory;

    public PerfIndexSeriesImpl(IndexFactoryImpl indexFactory, String name, IndexType indexType) {
        super(name, indexType);

        this.indexFactory = indexFactory;

        // detect configuration of series
        Config config = indexFactory.getConfig();
        InspectionIndexSeriesType indexSeriesConfig = null;
        for (InspectionIndexSeriesType idx : config.getIndexSeries()) {
            if (name.equals(idx.getName())) {
                indexSeriesConfig = idx;
                break;
            }
        }

        if (indexSeriesConfig == null) {
            indexSeriesConfig = config.getDefaultIndexSeries();
        }


        // set params from configuration
        supportHistogramm = coalesce(indexSeriesConfig.isAllowHistogramm(), config.getDefaultIndexSeries().isAllowHistogramm(), Boolean.FALSE);
        supportCPU = coalesce(indexSeriesConfig.isAllowCPU(), config.getDefaultIndexSeries().isAllowCPU(), Boolean.FALSE);
        ArrayList<String> childs = new ArrayList<String>();
        ChildSeriesType childSeries = indexSeriesConfig.getChildSeries();
        if (childSeries != null) {
            for (ChildIndexSeriesType child : childSeries.getChildIndexSeries()) {
                childs.add(child.getName());
            }
        }
        this.childSeries = childs.toArray(new String[childs.size()]);

    }

    private static <T> T coalesce(T... a) {
        for (T it : a) {
            if (it != null) {
                return it;
            }
        }
        return null;
    }

    public String[] getChildSeries() {
        return childSeries;
    }

    public boolean isSupportCPU() {
        return supportCPU;
    }

    public boolean isSupportHistogramm() {
        return supportHistogramm;
    }

    public static class IndexEvent {
        Counter counter;

        public long requestTime;

        Duration own = new Duration();
        public Duration[] childsDurations;

        public IndexEvent(int childsCount) {
            this.childsDurations = new Duration[childsCount];
            for (int i = 0; i < childsCount; i++) {
                childsDurations[i] = new Duration();
            }
        }
    }


    public Disruptor<IndexEvent> getDisruptor() {
        return indexFactory.getDisruptor();
    }
}

