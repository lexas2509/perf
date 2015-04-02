package org.lex.perf.report;

import org.apache.commons.codec.binary.Base64;
import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexSeries;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.engine.*;
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

    private static final IndexSeries GRAPH = IndexFactory.registerIndexSeries("PRF.RPT.GRPH");

    private void buildGraph(Range reportRange, GraphItemType graphItem, StringBuilder htmlReport) {
        GRAPH.bindContext(graphItem.getItem());
        try {
            RrdGraphDef graphDef = new RrdGraphDef();
            graphDef.setTimeSpan(reportRange.getStart().getTime() / 1000, reportRange.getEnd().getTime() / 1000);
            PerfIndexSeriesImpl category = ((IndexFactoryImpl) IndexFactory.getFactory()).getIndexSeries(graphItem.getCategory());
            if (category == null) {
                return;
            }

            IndexFactoryImpl impl = (IndexFactoryImpl) IndexFactory.getFactory();
            java.util.List<org.lex.perf.api.index.Index> indexes = impl.getIndexes(category);
            for (org.lex.perf.api.index.Index indexIt : indexes) {
                IndexImpl indexIt1 = (IndexImpl) indexIt;
                EngineIndex index = indexIt1.getIndex();
                switch (indexIt1.getIndexType()) {
                    case COUNTER:
                    case INSPECTION:
                        RrdCounter counter = (RrdCounter) index;
                        graphDef.datasource("hits", counter.getFileName(), "hits", ConsolFun.TOTAL);
                        graphDef.area("hits", new Color(30, 255, 18), "average of " + counter.getIndexName());
                        break;
                    case GAUGE:
                        RrdGauge gauge = (RrdGauge) index;
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
        } finally {
            GRAPH.unBindContext();
        }
    }

    private static final IndexSeries HISTOGRAM = IndexFactory.registerIndexSeries("PRF.RPT.HSGM");

    private void buildHistogramTable(Range reportRange, HistogramTableItemType reportItem, StringBuilder htmlReport) {
        HISTOGRAM.bindContext(reportItem.category);
        try {
            PerfIndexSeriesImpl category = ((IndexFactoryImpl)IndexFactory.getFactory()).getIndexSeries(reportItem.getCategory());
            if (category == null) {
                return;
            }
            if (!category.isSupportHistogramm()) {
                return;
            }
            htmlReport.append("<label>" + category.getName() + "</label>");
            htmlReport.append("<TABLE class=\"sortable, histogramm\" border=1 cellSpacing=0 summary=\"" + category.getName() + "\" cellPadding=2 width=\"100%\">");
            htmlReport.append("<THEAD>");
            htmlReport.append("<TR>");
            htmlReport.append("<TH>Request</TH>");
            htmlReport.append("<TH class=\"sorttable_numeric, column\">hits</TH>");
            htmlReport.append("<TH class=\"sorttable_numeric, column\">avg (ms)</TH>");
            for (int i = 0; i < CounterTimeSlot.times.length - 1; i++) {
                String s = "&lt;" + CounterTimeSlot.times[i + 1];
                htmlReport.append("<TH class=\"sorttable_numeric, column\">" + s + "ms</TH>");
            }

            htmlReport.append("<TH class=\"sorttable_numeric, column\">&gt;" + CounterTimeSlot.times[CounterTimeSlot.times.length - 1] + "ms </TH>");
            htmlReport.append("</TR>");
            htmlReport.append("</THEAD>");
            htmlReport.append("<TBODY>");
            IndexFactoryImpl impl = (IndexFactoryImpl) IndexFactory.getFactory();
            java.util.List<org.lex.perf.api.index.Index> indexes = impl.getIndexes(category);
            for (org.lex.perf.api.index.Index indexIt : indexes) {
                RrdIndex index = (RrdIndex)((IndexImpl) indexIt).getIndex();
                int slotDuration = index.getSlotDuration();
                long startTime = (reportRange.getStart().getTime() / slotDuration) * slotDuration / 1000;
                long endTime = (reportRange.getEnd().getTime() / slotDuration) * slotDuration / 1000 - 1;
                DataProcessor dp = new DataProcessor(startTime, endTime);
                dp.setStep(slotDuration / 1000);
                switch (indexIt.getIndexType()) {
                    case COUNTER:
                    case INSPECTION:
                        RrdCounter counter = (RrdCounter) index;
                        dp.addDatasource("hits", counter.getFileName(), "hits", ConsolFun.TOTAL);
                        dp.addDatasource("total", counter.getFileName(), "total", ConsolFun.TOTAL);

                        for (int i = 0; i < CounterTimeSlot.times.length; i++) {
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
                        htmlReport.append("<TD>" + indexIt.getName() + "</TD>");
                        htmlReport.append("<TD class=\"numeric\">" + String.format("%16.0f", hits) + "</TD>");
                        htmlReport.append("<TD class=\"numeric\">" + String.format("%16.3f", average / 1000 / 1000) + "</TD>");

                        for (int i = 0; i < CounterTimeSlot.times.length; i++) {
                            double result = 0;
                            for (int j = 0; j < cnt; j++) {
                                double v = values[i + 2][j];
                                if (Double.isNaN(v)) {
                                    v = 0;
                                }
                                result = result + v;
                            }

                            htmlReport.append("<TD class=\"numeric\">" + String.format("%16.0f", result) + "</TD>");
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
        } finally {
            HISTOGRAM.unBindContext();
        }
    }

    private static final IndexSeries TABLE = IndexFactory.registerIndexSeries("PRF.RPT.TBL");

    private void buildPerfTable(Range reportRange, PerfTableItemType reportItem, StringBuilder htmlReport) {
        TABLE.bindContext(reportItem.category);
        try {
            PerfIndexSeriesImpl indexSeries = ((IndexFactoryImpl)IndexFactory.getFactory()).getIndexSeries(reportItem.getCategory());
            if (indexSeries == null) {
                return;
            }
            htmlReport.append("<label>" + indexSeries.getName() + "</label>");
            htmlReport.append("<TABLE class=sortable border=1 cellSpacing=0 summary=\"" + indexSeries.getName() + "\" cellPadding=2 width=\"100%\">");
            htmlReport.append("<THEAD>");
            htmlReport.append("<TR>");

            StringBuilder firstHead = new StringBuilder();
            StringBuilder secondHead = new StringBuilder();
            int colspan = 0;
            secondHead.append("<TH class=sorttable_string>name</TH>");
            secondHead.append("<TH class=sorttable_numeric>hits</TH>");
            secondHead.append("<TH class=sorttable_numeric>total (s)</TH>");
            secondHead.append("<TH class=sorttable_numeric>avg (ms)</TH>");
            colspan += 4;
            if (indexSeries.isSupportCPU()) {
                secondHead.append("<TH class=sorttable_numeric>total cpu (s)</TH>");
                secondHead.append("<TH class=sorttable_numeric>cpu avg (ms)</TH>");
                colspan += 2;
            }

            firstHead.append("<TH colSpan=\"" + Integer.toString(colspan) + "\">" + indexSeries.getName() + "</TH>");

            IndexFactoryImpl factory = (IndexFactoryImpl) IndexFactory.getFactory();
            for (String ixName : indexSeries.getChildSeries()) {
                colspan = 0;
                secondHead.append("<TH class=sorttable_numeric>hits</TH>");
                secondHead.append("<TH class=sorttable_numeric>total (s)</TH>");
                secondHead.append("<TH class=sorttable_numeric>avg (ms)</TH>");
                colspan += 3;
                if (factory.isCpuSupported(ixName)) {
                    secondHead.append("<TH class=sorttable_numeric>total cpu (s)</TH>");
                    secondHead.append("<TH class=sorttable_numeric>cpu avg (ms)</TH>");
                    colspan += 2;
                }
                firstHead.append("<TH colSpan=\"" + Integer.toString(colspan) + "\">" + ixName + "</TH>");
            }
            htmlReport.append(firstHead);
            htmlReport.append("</TR>");
            htmlReport.append("<TR>");
            htmlReport.append(secondHead);
            htmlReport.append("</TR>");
            htmlReport.append("</THEAD>");
            htmlReport.append("<TBODY>");
            IndexFactoryImpl impl = (IndexFactoryImpl) IndexFactory.getFactory();
            java.util.List<org.lex.perf.api.index.Index> indexes = impl.getIndexes(indexSeries);
            for (org.lex.perf.api.index.Index indexIt : indexes) {
                RrdIndex index = (RrdIndex)((IndexImpl) indexIt).getIndex();
                int slotDuration = index.getSlotDuration();
                long startTime = (reportRange.getStart().getTime() / slotDuration) * slotDuration / 1000;
                long endTime = (reportRange.getEnd().getTime() / slotDuration) * slotDuration / 1000 - 1;
                DataProcessor dp = new DataProcessor(startTime, endTime);
                dp.setStep(slotDuration / 1000);
                switch (indexIt.getIndexType()) {
                    case COUNTER:
                    case INSPECTION:
                        RrdCounter counter = (RrdCounter) index;
                        dp.addDatasource("hits", counter.getFileName(), "hits", ConsolFun.TOTAL);
                        dp.addDatasource("total", counter.getFileName(), "total", ConsolFun.TOTAL);
                        if (indexSeries.isSupportCPU()) {
                            dp.addDatasource("totalcpu", counter.getFileName(), "totalcpu", ConsolFun.TOTAL);
                        }

                        for (String ixName : indexSeries.getChildSeries()) {
                            dp.addDatasource(ixName + "_hits", counter.getFileName(), ixName + "_hits", ConsolFun.TOTAL);
                            dp.addDatasource(ixName + "_total", counter.getFileName(), ixName + "_total", ConsolFun.TOTAL);

                            if (factory.isCpuSupported(ixName)) {
                                dp.addDatasource(ixName + "_totalcpu", counter.getFileName(), ixName + "_totalcpu", ConsolFun.TOTAL);
                            }
                        }


                        try {
                            dp.processData();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        double[][] values = dp.getValues();
                        double[] res = sumTotal(values);

                        int idx = 0;
                        htmlReport.append("<TR onmouseover=\"this.className='highlight'\" onmouseout=\"this.className=''\">");
                        htmlReport.append("<TD>" + index.getIndexName() + "</TD>");
                        boolean supportCPU = indexSeries.isSupportCPU();

                        double hits = res[idx];
                        idx = append(htmlReport, supportCPU, res, idx, 1);


                        for (String ixName : indexSeries.getChildSeries()) {
                            idx = append(htmlReport, factory.isCpuSupported(ixName), res, idx, hits);
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
        } finally {
            TABLE.unBindContext();
        }
    }

    private int append(StringBuilder htmlReport, boolean supportCPU, double[] res, int idx, double multiply) {
        double hits = res[idx++];
        htmlReport.append("<TD>" + String.format("%16.0f", hits / (multiply == 0 ? 1 : multiply)) + "</TD>");
        double total = res[idx++];
        htmlReport.append("<TD>" + String.format("%16.3f", total / 1000 / 1000 / 1000) + "</TD>");
        double average = hits == 0 ? 0 : total / hits;
        htmlReport.append("<TD>" + String.format("%16.3f", average / 1000 / 1000) + "</TD>");
        if (supportCPU) {
            double totalCPU = res[idx++];
            htmlReport.append("<TD>" + String.format("%16.3f", totalCPU / 1000 / 1000 / 1000) + "</TD>");
            double averageCPU = hits == 0 ? 0 : totalCPU / hits;
            htmlReport.append("<TD>" + String.format("%16.3f", averageCPU / 1000 / 1000) + "</TD>");
        }
        return idx;
    }

    private double[] sumTotal(double[][] values) {
        double[] res = new double[values.length];
        int cnt = values[0].length;
        for (int j = 0; j < values.length; j++) {
            double val = 0;
            for (int i = 0; i < cnt; i++) {

                double v = values[j][i];
                if (Double.isNaN(v)) {
                    v = 0;
                }
                val = val + v;
            }
            res[j] = val;
        }
        return res;
    }
}
