package org.lex.perf.web;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.lex.perf.engine.Counter;
import org.lex.perf.engine.Engine;
import org.lex.perf.engine.Gauge;
import org.lex.perf.event.MonitoringCategory;
import org.lex.perf.report.GraphItemType;
import org.lex.perf.report.Report;
import org.lex.perf.report.ReportItemType;
import org.rrd4j.ConsolFun;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

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
        graphDef.setTimeSpan(reportRange.getStart().getTime() / 1000 - 3600, reportRange.getEnd().getTime() / 1000);
        MonitoringCategory category = MonitoringCategory.get(graphItem.getCategory());
        switch (category.getCategoryType()) {
            case COUNTER:
                Counter counter = Engine.engine.getCounter(category, graphItem.getItem());
                graphDef.datasource("hits", counter.getFileName(), "hits", ConsolFun.TOTAL);
                graphDef.datasource("value", counter.getFileName(), "value", ConsolFun.TOTAL);
                graphDef.datasource("average", "value,hits,/");
                graphDef.line("average", new Color(0xFF, 0, 0), "average", 2);
                break;
            case GAUGE:
                Gauge gauge = Engine.engine.getGauge(category, graphItem.getItem());
                graphDef.datasource("value", gauge.getFileName(), "value", ConsolFun.AVERAGE);
                graphDef.line("value", new Color(0xFF, 0, 0), "value", 2);
                break;
            default:
                break;
        }
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
}
