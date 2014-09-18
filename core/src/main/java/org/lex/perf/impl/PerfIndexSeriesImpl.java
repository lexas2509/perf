package org.lex.perf.impl;

import org.lex.perf.api.factory.IndexSeriesImpl;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.config.ChildIndexSeriesType;
import org.lex.perf.config.Config;
import org.lex.perf.config.InspectionIndexSeriesType;

import java.util.ArrayList;

/**
 * Created by Алексей on 18.09.2014.
 */
public class PerfIndexSeriesImpl extends IndexSeriesImpl {

    private String[] childSeries;
    private final Boolean supportCPU;
    private boolean supportHistogramm;

    public PerfIndexSeriesImpl(IndexFactoryImpl indexFactory, String name, IndexType indexType) {
        super(name, indexType);
        Config config = indexFactory.getConfig();
        InspectionIndexSeriesType indexSeriesConfig = config.getDefaultIndexSeries();
        for (InspectionIndexSeriesType idx : config.getIndexSeries()) {
            if (name.equals(idx.getName())) {
                indexSeriesConfig = idx;
            }
        }


        supportHistogramm = indexSeriesConfig.isAllowHistogramm();
        supportCPU = indexSeriesConfig.isAllowCPU();
        ArrayList<String> childs = new ArrayList<String>();
        for (ChildIndexSeriesType child : indexSeriesConfig.getChildIndexSeries()) {
            childs.add(child.getName());
        }
        childSeries = childs.toArray(new String[childs.size()]);
    }

    public String[] getChildSeries() {
        return childSeries;
    }

    public boolean isSupportCPU() {
        return true;
    }

    public boolean isSupportHistogramm() {
        return supportHistogramm;
    }
}
