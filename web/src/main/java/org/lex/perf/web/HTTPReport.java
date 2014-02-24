package org.lex.perf.web;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.rrd4j.ConsolFun;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 */
public class HTTPReport extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //super.doGet(req, resp);
        String base64image = getAverage();
        String base64Hits = getHits();
        String reportAverage = "<img alt=\"graph\" src=\"data:image/png;base64," + base64image + "\" />";
        String reportHits = "<img alt=\"graph\" src=\"data:image/png;base64," + base64Hits + "\" />";
        resp.setStatus(HttpServletResponse.SC_OK);
        OutputStream os = resp.getOutputStream();
        String result = "<html><body>" + reportAverage + "<br/>" + reportHits + "</body></html>";
        os.write(result.getBytes());
        os.close();

    }

    private String getAverage() throws IOException {
        RrdGraphDef graphDef = new RrdGraphDef();
        graphDef.setTimeSpan(System.currentTimeMillis() / 1000 - 3600, System.currentTimeMillis() / 1000);
        graphDef.datasource("hits", "e:/test.rrd", "hits", ConsolFun.TOTAL);
        graphDef.datasource("value", "e:/test.rrd", "value", ConsolFun.TOTAL);
        graphDef.datasource("average", "value,hits,/");
        graphDef.line("average", new Color(0xFF, 0, 0), "average", 2);
        //graphDef.line("value", new Color(0xFF, 0, 0), "value", 2);
        graphDef.setFilename("-");
        RrdGraph graph = new RrdGraph(graphDef);
        byte[] img = graph.getRrdGraphInfo().getBytes();

        return Base64.encode(img);
    }

    private String getHits() throws IOException {
        RrdGraphDef graphDef = new RrdGraphDef();
        graphDef.setTimeSpan(System.currentTimeMillis() / 1000 - 3600, System.currentTimeMillis() / 1000);
        graphDef.datasource("hits", "e:/test.rrd", "hits", ConsolFun.TOTAL);
        graphDef.area("hits", new Color(0xFF, 0, 0), "hits");
        graphDef.setFilename("-");
        RrdGraph graph = new RrdGraph(graphDef);
        byte[] img = graph.getRrdGraphInfo().getBytes();

        return Base64.encode(img);
    }

}
