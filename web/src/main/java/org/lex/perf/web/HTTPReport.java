package org.lex.perf.web;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.lex.perf.engine.Counter;
import org.lex.perf.engine.Engine;
import org.lex.perf.engine.Gauge;
import org.lex.perf.engine.Index;
import org.lex.perf.event.MonitoringCategory;
import org.lex.perf.report.GraphItemType;
import org.lex.perf.report.Report;
import org.lex.perf.report.ReportItemType;
import org.lex.perf.report.TableItemType;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.FetchData;
import org.rrd4j.data.DataProcessor;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

/**
 */
public class HTTPReport extends HttpServlet {

    public static class Range {
        private Date start;
        private Date end;

        public Range(Date start, Date end) {
            this.start = start;
            this.end = end;
        }

        public Date getStart() {
            return start;
        }

        public Date getEnd() {
            return end;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Date now = new Date();
        Range reportRange = new Range(new Date(now.getTime() - 3600 * 1000), now);
        try {
            JAXBContext ctx = JAXBContext.newInstance("org.lex.perf.report");
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            JAXBElement<Report> res = (JAXBElement<Report>) unmarshaller.unmarshal(this.getClass().getClassLoader().getResource("defaultReport.xml"));
            Report report = res.getValue();
            StringBuilder htmlReport = new StringBuilder();
            htmlReport.append("<html><body>");
            for (ReportItemType reportItem : report.getGraphOrTable()) {
                if (reportItem instanceof GraphItemType) {
                    buildGraph(reportRange, (GraphItemType) reportItem, htmlReport);
                }
                if (reportItem instanceof TableItemType) {
                    buildTable(reportRange, (TableItemType) reportItem, htmlReport);
                }
                htmlReport.append("</br>");
            }

            htmlReport.append("</body></html>");
            resp.setStatus(HttpServletResponse.SC_OK);
            OutputStream os = resp.getOutputStream();

            os.write(htmlReport.toString().getBytes());
            os.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void buildGraph(Range reportRange, GraphItemType graphItem, StringBuilder htmlReport) {
        RrdGraphDef graphDef = new RrdGraphDef();
        graphDef.setTimeSpan(reportRange.getStart().getTime() / 1000, reportRange.getEnd().getTime() / 1000);
        MonitoringCategory category = MonitoringCategory.get(graphItem.getCategory());
        Index index = Engine.engine.getIndex(category, graphItem.getItem());
        switch (category.getCategoryType()) {
            case COUNTER:
                Counter counter = (Counter) index;
                graphDef.datasource("hits", "e:/mondata/" + counter.getFileName() + ".rrd", "hits", ConsolFun.TOTAL);
                //graphDef.datasource("value", "e:/mondata/" + counter.getFileName() + ".rrd", "value", ConsolFun.TOTAL);
                //graphDef.datasource("average", "value,hits,/");
                graphDef.area("hits", new Color(30, 255, 18), "average of " + counter.getIndexName());
                //graphDef.area("average", new Color(0xFF, 0, 0), "average of " + counter.getIndexName());
                break;
            case GAUGE:
                Gauge gauge = (Gauge) index;
                graphDef.datasource("value", "e:/mondata/" + gauge.getFileName() + ".rrd", "value", ConsolFun.AVERAGE);
                graphDef.datasource("minvalue", "e:/mondata/" + gauge.getFileName() + ".rrd", "value", ConsolFun.MIN);
                graphDef.datasource("maxvalue", "e:/mondata/" + gauge.getFileName() + ".rrd", "value", ConsolFun.MAX);
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
        String reportGraph = "<img alt=\"graph\" src=\"data:image/png;base64," + Base64.encode(img) + "\" />";
        htmlReport.append(reportGraph);
    }

    private void buildTable(Range reportRange, TableItemType reportItem, StringBuilder htmlReport) {
        MonitoringCategory category = MonitoringCategory.get(reportItem.getCategory());

        htmlReport.append("<TABLE class=sortable border=1 cellSpacing=0 summary=\"" + category.getName() + "\" cellPadding=2 width=\"100%\">");
        htmlReport.append("<THEAD>");
        htmlReport.append("<TR>");
        htmlReport.append("<TH>Request</TH>");
        htmlReport.append("<TH class=sorttable_numeric>hits</TH>");
        htmlReport.append("<TH class=sorttable_numeric>avg</TH>");
        htmlReport.append("</TR>");
        htmlReport.append("</THEAD>");
        htmlReport.append("<TBODY>");

        List<Index> indexes = Engine.engine.getIndexes(category);
        for (Index index : indexes) {
            int slotDuration = index.getSlotDuration();
            long startTime = (reportRange.getStart().getTime() / slotDuration) * slotDuration / 1000;
            long endTime = (reportRange.getEnd().getTime() / slotDuration) * slotDuration / 1000 -1;
            DataProcessor dp = new DataProcessor(startTime, endTime);
            dp.setStep(slotDuration/1000);
            switch (category.getCategoryType()) {
                case COUNTER:
                    Counter counter = (Counter) index;
                    dp.addDatasource("hits", "e:/mondata/" + counter.getFileName() + ".rrd", "hits", ConsolFun.TOTAL);
                    dp.addDatasource("total", "e:/mondata/" + counter.getFileName() + ".rrd", "total", ConsolFun.TOTAL);
                    dp.addDatasource("_hits", "hits, UN, 0, hits, IF");
                    dp.addDatasource("_total", "total, UN, 0, total, IF");
                    double hits = 0;
                    double total = 0;
                    try {
                        dp.processData();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    double[][] values = dp.getValues();
                    int cnt = values[0].length;
                    for (int i =0; i <cnt; i ++) {
                        hits = hits + values[2][i];
                        total = total + values[3][i];
                    }
                    double average = hits == 0 ? 0 : total / hits;
                    System.out.println("hits: " + hits + " total " + total + " avg " + average);

                    htmlReport.append("<TR onmouseover=\"this.className='highlight'\" onmouseout=\"this.className=''\">");
                    htmlReport.append("<TD>" + index.getIndexName() +"</TD>");
                    htmlReport.append("<TD>" + Double.toString(hits) + "</TD>");
                    htmlReport.append("<TD>" + Double.toString(average) + "</TD>");
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
