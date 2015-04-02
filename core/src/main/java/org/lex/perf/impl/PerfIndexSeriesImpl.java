package org.lex.perf.impl;

import com.lmax.disruptor.dsl.Disruptor;
import org.lex.perf.api.factory.IndexSeriesImpl;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.api.index.InspectionIndex;
import org.lex.perf.config.ChildIndexSeriesType;
import org.lex.perf.config.ChildSeriesType;
import org.lex.perf.config.Config;
import org.lex.perf.config.InspectionIndexSeriesType;
import org.lex.perf.engine.IndexEvent;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Алексей on 18.09.2014.
 */
public class PerfIndexSeriesImpl extends IndexSeriesImpl {


    private String[] childSeries;
    private final Boolean supportCPU;
    private boolean supportHistogramm;

    private IndexFactoryImpl indexFactory;

    public final static ThreadLocal<Stack<InspectionIndex>> inspections = new ThreadLocal<Stack<InspectionIndex>>() {
        @Override
        protected Stack<InspectionIndex> initialValue() {
            return new Stack<InspectionIndex>();
        }
    };


    public PerfIndexSeriesImpl(IndexFactoryImpl indexFactory, String name) {
        super(name);

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

    public String getName() {
        return name;
    }

    @Override
    public void bindContext(String contextName) {
        InspectionIndex index = (InspectionIndex) indexFactory.getIndex(this, contextName, IndexType.INSPECTION);
        inspections.get().push(index);
        index.bindContext();
    }

    @Override
    public void unBindContext() {
        InspectionIndex index = inspections.get().pop();
        index.unBindContext();
    }


    public Disruptor<IndexEvent> getDisruptor() {
        return indexFactory.getDisruptor();
    }
}

