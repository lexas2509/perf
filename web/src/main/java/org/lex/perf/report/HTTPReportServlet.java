package org.lex.perf.report;

import org.lex.perf.web.ClassLoaderResourceHttpItem;
import org.lex.perf.web.HTTPServlet;
import org.lex.perf.web.RedirectHttpItem;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 */
public class HTTPReportServlet extends HTTPServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);


        final String defaultRedirect = path + "/mainReport.html";

        // standart redirect from root to main page with report
        httpItems.put("", new RedirectHttpItem(defaultRedirect));

        // mainPage
        httpItems.put("/mainReport.html", new ClassLoaderResourceHttpItem("org/lex/perf/web/report/mainReport.html"));

        httpItems.put("/currentTime", new CurrentTime());
        // report
        httpItems.put("/report.html", new PerformanceReport());


        // additional resources, used in mainreport
        httpItems.put("/css/style.css", new ClassLoaderResourceHttpItem("org/lex/perf/web/report/style.css"));
        httpItems.put("/css/jquery-ui.css", new ClassLoaderResourceHttpItem("org/lex/perf/web/report/jquery-ui.css"));
        httpItems.put("/js/jquery-1.9.1.js", new ClassLoaderResourceHttpItem("org/lex/perf/web/report/jquery-1.9.1.js"));
        httpItems.put("/js/jquery-ui.js", new ClassLoaderResourceHttpItem("org/lex/perf/web/report/jquery-ui.js"));
    }
}
