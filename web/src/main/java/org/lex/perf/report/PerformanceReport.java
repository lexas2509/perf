package org.lex.perf.report;

import org.apache.commons.codec.binary.Base64;
import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.engine.Counter;
import org.lex.perf.engine.CounterTimeSlot;
import org.lex.perf.engine.Gauge;
import org.lex.perf.engine.Index;
import org.lex.perf.impl.IndexFactoryImpl;
import org.lex.perf.impl.IndexImpl;
import org.lex.perf.impl.PerfIndexSeriesImpl;
import org.lex.perf.util.JAXBUtil;
import org.lex.perf.web.HttpItem;
import org.rrd4j.ConsolFun;
import org.rrd4j.data.DataProcessor;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * User: lexas
 * Date: 04.04.14
 * Time: 21:24
 */
public class PerformanceReport implements HttpItem {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse response) {
        try {
            Date startDate;
            Date endDate;
            try {
                if (req.getParameterMap().containsKey("start") && req.getParameterMap().containsKey("end")) {
                    long start = Long.parseLong(req.getParameter("start"));
                    long end = Long.parseLong(req.getParameter("end"));
                    startDate = new Date(start * 1000);
                    endDate = new Date(end * 1000);
                } else {
                    throw new RuntimeException("skip");
                }
            } catch (Exception e) {
                long currentTime = System.currentTimeMillis();
                currentTime = currentTime - currentTime % 1000; // align to second
                startDate = new Date(currentTime - 1 * 60 * 60 * 1000);
                endDate = new Date(currentTime);
            }
            StringBuilder htmlReport = getHtmlReport(startDate, endDate);

            response.setStatus(HttpServletResponse.SC_OK);
            OutputStream os = response.getOutputStream();

            os.write(htmlReport.toString().getBytes());
            os.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private StringBuilder getHtmlReport(Date startDate, Date endDate) throws JAXBException {
        Range reportRange = new Range(startDate, endDate);
        String contextPath = "org.lex.perf.report";
        String name = "defaultReport.xml";
        Report report = JAXBUtil.getObject(contextPath, name);
        StringBuilder htmlReport = new StringBuilder();
        htmlReport.append("<html>");
        htmlReport.append("<body>");

        StringBuilder data = getDataPart(reportRange, report);

        htmlReport.append(data);
        htmlReport.append("</body>");
        htmlReport.append("</html>");
        return htmlReport;
    }

    private StringBuilder getDataPart(Range reportRange, Report report) {
        StringBuilder data = new StringBuilder();
        for (ReportItemType reportItem : report.getGraphOrHistogramTableOrPerfTable()) {
            if (reportItem instanceof GraphItemType) {
                buildGraph(reportRange, (GraphItemType) reportItem, data);
            }
            if (reportItem instanceof HistogramTableItemType) {
                buildHistogramTable(reportRange, (HistogramTableItemType) reportItem, data);
            }
            if (reportItem instanceof PerfTableItemType) {
                buildPerfTable(reportRange, (PerfTableItemType) reportItem, data);
            }
            data.append("</br>");
        }
        return data;
    }

    private void buildGraph(Range reportRange, GraphItemType graphItem, StringBuilder htmlReport) {
        RrdGraphDef graphDef = new RrdGraphDef();
        graphDef.setTimeSpan(reportRange.getStart().getTime() / 1000, reportRange.getEnd().getTime() / 1000);
        IndexSeries category = IndexFactory.getIndexSeries(graphItem.getCategory());
        if (category == null) {
            return;
        }

        IndexFactoryImpl impl = (IndexFactoryImpl) IndexFactory.getFactory();
        java.util.List<org.lex.perf.api.index.Index> indexes = impl.getIndexes(category);
        for (org.lex.perf.api.index.Index indexIt : indexes) {
            Index index = ((IndexImpl) indexIt).getIndex();
            switch (category.getIndexType()) {
                case COUNTER:
                case INSPECTION:
                    Counter counter = (Counter) index;
                    graphDef.datasource("hits", counter.getFileName(), "hits", ConsolFun.TOTAL);
                    //graphDef.datasource("value", "e:/mondata/" + counter.getFileName() + ".rrd", "value", ConsolFun.TOTAL);
                    //graphDef.datasource("average", "value,hits,/");
                    graphDef.area("hits", new Color(30, 255, 18), "average of " + counter.getIndexName());
                    //graphDef.area("average", new Color(0xFF, 0, 0), "average of " + counter.getIndexName());
                    break;
                case GAUGE:
                    Gauge gauge = (Gauge) index;
                    graphDef.datasource("value", gauge.getFileName(), "value", ConsolFun.AVERAGE);
                    graphDef.datasource("minvalue", gauge.getFileName(), "value", ConsolFun.MIN);
                    graphDef.datasource("maxvalue", gauge.getFileName(), "value", ConsolFun.MAX);
                    graphDef.line("maxvalue", new Color(255, 6, 25), gauge.getIndexName(), 1);
                    graphDef.area("minvalue", new Color(1, 5, 255), gauge.getIndexName());
                    graphDef.area("value", new Color(8, 255, 6), gauge.getIndexName());
                    break;
                default:
                    break;
            }
            graphDef.setHeight(160);
            graphDef.setWidth(400);
            graphDef.setFilename("-");
            RrdGraph graph = null;
            try {
                graph = new RrdGraph(graphDef);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            byte[] img = graph.getRrdGraphInfo().getBytes();
            String reportGraph = "<img alt=\"graph\" src=\"data:image/png;base64," + Base64.decodeBase64(img) + "\" />";
            htmlReport.append(reportGraph);
        }
    }

    private void buildHistogramTable(Range reportRange, HistogramTableItemType reportItem, StringBuilder htmlReport) {
        PerfIndexSeriesImpl category = (PerfIndexSeriesImpl) IndexFactory.getIndexSeries(reportItem.getCategory());
        if (category == null) {
            return;
        }
        if (!category.isSupportHistogramm()) {
            return;
        }
        htmlReport.append("<label>" + category.getName() + "</label>");
        htmlReport.append("<TABLE class=sortable border=1 cellSpacing=0 summary=\"" + category.getName() + "\" cellPadding=2 width=\"100%\">");
        htmlReport.append("<THEAD>");
        htmlReport.append("<TR>");
        htmlReport.append("<TH>Request</TH>");
        htmlReport.append("<TH class=sorttable_numeric>hits</TH>");
        htmlReport.append("<TH class=sorttable_numeric>avg (ms)</TH>");
        for (int i = 0; i < CounterTimeSlot.times.length - 1; i++) {
            String s = "&lt;" + CounterTimeSlot.times[i + 1];
            htmlReport.append("<TH class=sorttable_numeric>" + s + "ms</TH>");
        }

        htmlReport.append("<TH class=sorttable_numeric>&gt;" + CounterTimeSlot.times[CounterTimeSlot.times.length - 1] + "ms </TH>");
        htmlReport.append("</TR>");
        htmlReport.append("</THEAD>");
        htmlReport.append("<TBODY>");
        IndexFactoryImpl impl = (IndexFactoryImpl) IndexFactory.getFactory();
        java.util.List<org.lex.perf.api.index.Index> indexes = impl.getIndexes(category);
        for (org.lex.perf.api.index.Index indexIt : indexes) {
            Index index = ((IndexImpl) indexIt).getIndex();
            int slotDuration = index.getSlotDuration();
            long startTime = (reportRange.getStart().getTime() / slotDuration) * slotDuration / 1000;
            long endTime = (reportRange.getEnd().getTime() / slotDuration) * slotDuration / 1000 - 1;
            DataProcessor dp = new DataProcessor(startTime, endTime);
            dp.setStep(slotDuration / 1000);
            switch (category.getIndexType()) {
                case COUNTER:
                case INSPECTION:
                    Counter counter = (Counter) index;
                    dp.addDatasource("hits", counter.getFileName(), "hits", ConsolFun.TOTAL);
                    dp.addDatasource("total", counter.getFileName(), "total", ConsolFun.TOTAL);

                    for (int i = 0; i < CounterTimeSlot.times.length + 1; i++) {
                        dp.addDatasource("hits" + Integer.toString(i), counter.getFileName(), "hits" + Integer.toString(i), ConsolFun.TOTAL);
                    }
                    double hits = 0;
                    double total = 0;
                    try {
                        dp.processData();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    double[][] values = dp.getValues();
                    int cnt = values[0].length;
                    for (int i = 0; i < cnt; i++) {
                        double v = values[0][i];
                        if (Double.isNaN(v)) {
                            v = 0;
                        }
                        hits = hits + v;
                        double v1 = values[1][i];
                        if (Double.isNaN(v1)) {
                            v1 = 0;
                        }
                        total = total + v1;
                    }
                    double average = hits == 0 ? 0 : total / hits;

                    htmlReport.append("<TR onmouseover=\"this.className='highlight'\" onmouseout=\"this.className=''\">");
                    htmlReport.append("<TD>" + index.getIndexName() + "</TD>");
                    htmlReport.append("<TD>" + String.format("%16.0f", hits) + "</TD>");
                    htmlReport.append("<TD>" + String.format("%16.3f", average / 1000 / 1000) + "</TD>");

                    for (int i = 0; i < CounterTimeSlot.times.length + 1; i++) {
                        double result = 0;
                        for (int j = 0; j < cnt; j++) {
                            double v = values[i + 2][j];
                            if (Double.isNaN(v)) {
                                v = 0;
                            }
                            result = result + v;
                        }

                        htmlReport.append("<TD>" + String.format("%16.0f", result) + "</TD>");
                    }
                    htmlReport.append("</TR>");
                    break;
                case GAUGE:
                    break;
                default:
                    break;
            }
        }
        htmlReport.append("</TBODY>");
        htmlReport.append("</TABLE>");
    }

    private void buildPerfTable(Range reportRange, PerfTableItemType reportItem, StringBuilder htmlReport) {
        IndexSeries category = IndexFactory.getIndexSeries(reportItem.getCategory());
        if (category == null) {
            return;
        }
        if (!(category instanceof PerfIndexSeriesImpl)) {
            return;
        }
        PerfIndexSeriesImpl indexSeries = (PerfIndexSeriesImpl) category;
        htmlReport.append("<label>" + category.getName() + "</label>");
        htmlReport.append("<TABLE class=sortable border=1 cellSpacing=0 summary=\"" + category.getName() + "\" cellPadding=2 width=\"100%\">");
        htmlReport.append("<THEAD>");
        htmlReport.append("<TR>");
        htmlReport.append("<TH>Request</TH>");
        htmlReport.append("<TH class=sorttable_numeric>hits</TH>");
        htmlReport.append("<TH class=sorttable_numeric>total (ms)</TH>");
        htmlReport.append("<TH class=sorttable_numeric>avg (ms)</TH>");
        if (indexSeries.isSupportCPU()) {
            htmlReport.append("<TH class=sorttable_numeric>total cpu (ms)</TH>");
            htmlReport.append("<TH class=sorttable_numeric>cpu avg (ms)</TH>");
        }

        for (String ixName : indexSeries.getChildSeries()) {
            PerfIndexSeriesImpl ix = (PerfIndexSeriesImpl) IndexFactory.getIndexSeries(ixName);
            if (ix != null) {
                htmlReport.append("<TH class=sorttable_numeric>hits</TH>");
                htmlReport.append("<TH class=sorttable_numeric>total (ms)</TH>");
                htmlReport.append("<TH class=sorttable_numeric>avg (ms)</TH>");
                if (ix.isSupportCPU()) {
                    htmlReport.append("<TH class=sorttable_numeric>total cpu (ms)</TH>");
                    htmlReport.append("<TH class=sorttable_numeric>cpu avg (ms)</TH>");
                }
            }
        }
        htmlReport.append("</TR>");
        htmlReport.append("</THEAD>");
        htmlReport.append("<TBODY>");
        IndexFactoryImpl impl = (IndexFactoryImpl) IndexFactory.getFactory();
        java.util.List<org.lex.perf.api.index.Index> indexes = impl.getIndexes(category);
        for (org.lex.perf.api.index.Index indexIt : indexes) {
            Index index = ((IndexImpl) indexIt).getIndex();
            int slotDuration = index.getSlotDuration();
            long startTime = (reportRange.getStart().getTime() / slotDuration) * slotDuration / 1000;
            long endTime = (reportRange.getEnd().getTime() / slotDuration) * slotDuration / 1000 - 1;
            DataProcessor dp = new DataProcessor(startTime, endTime);
            dp.setStep(slotDuration / 1000);
            switch (category.getIndexType()) {
                case COUNTER:
                case INSPECTION:
                    Counter counter = (Counter) index;
                    dp.addDatasource("hits", counter.getFileName(), "hits", ConsolFun.TOTAL);
                    dp.addDatasource("total", counter.getFileName(), "total", ConsolFun.TOTAL);
                    if (indexSeries.isSupportCPU()) {
                        dp.addDatasource("totalcpu", counter.getFileName(), "totalcpu", ConsolFun.TOTAL);
                    }

                    for (String ixName : indexSeries.getChildSeries()) {
                        PerfIndexSeriesImpl ix = (PerfIndexSeriesImpl) IndexFactory.getIndexSeries(ixName);
                        if (ix != null) {
                            dp.addDatasource(ixName + "_hits", counter.getFileName(), ixName + "_hits", ConsolFun.TOTAL);
                            dp.addDatasource(ixName + "_total", counter.getFileName(), ixName + "_total", ConsolFun.TOTAL);

                            if (ix.isSupportCPU()) {
                                dp.addDatasource(ixName + "_totalcpu", counter.getFileName(), ixName + "_totalcpu", ConsolFun.TOTAL);
                            }
                        }
                    }


                    double hits = 0;
                    double total = 0;
                    double totalCPU = 0;
                    try {
                        dp.processData();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    double[][] values = dp.getValues();
                    int cnt = values[0].length;
                    for (int i = 0; i < cnt; i++) {

                        double v = values[0][i];
                        if (Double.isNaN(v)) {
                            v = 0;
                        }
                        hits = hits + v;

                        double v1 = values[1][i];
                        if (Double.isNaN(v1)) {
                            v1 = 0;
                        }
                        total = total + v1;

                        if (indexSeries.isSupportCPU()) {
                            double v2 = values[2][i];
                            if (Double.isNaN(v2)) {
                                v2 = 0;
                            }
                            totalCPU = totalCPU + v2;
                        }
                    }
                    double average = hits == 0 ? 0 : total / hits;

                    htmlReport.append("<TR onmouseover=\"this.className='highlight'\" onmouseout=\"this.className=''\">");
                    htmlReport.append("<TD>" + index.getIndexName() + "</TD>");
                    htmlReport.append("<TD>" + String.format("%16.0f", hits) + "</TD>");
                    htmlReport.append("<TD>" + String.format("%16.3f", average / 1000 / 1000) + "</TD>");
                    htmlReport.append("<TD>" + String.format("%16.3f", average / 1000 / 1000) + "</TD>");

                    if (indexSeries.isSupportCPU()) {
                        double averageCPU = hits == 0 ? 0 : totalCPU / hits;
                        htmlReport.append("<TD>" + String.format("%16.3f", averageCPU / 1000 / 1000) + "</TD>");
                    }

                    htmlReport.append("</TR>");
                    break;
                case GAUGE:
                    break;
                default:
                    break;
            }
        }
        htmlReport.append("</TBODY>");
        htmlReport.append("</TABLE>");

    }


}
