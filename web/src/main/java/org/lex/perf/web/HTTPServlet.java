package org.lex.perf.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class HTTPServlet extends HttpServlet {

    protected String path;

    protected Map<String, HttpItem> httpItems = new HashMap<String, HttpItem>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        path = config.getServletContext().getContextPath() + "/" + config.getServletName();
    }

        @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String queryString = req.getRequestURI();
            if (queryString.startsWith((path))) {
                queryString = queryString.substring(path.length());
                HttpItem item = httpItems.get(queryString);
                if (item != null) {
                    item.doGet(req, resp);
                } else {
                    sendNotFound(resp);
                }
            } else {
                sendNotFound(resp);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendNotFound(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        OutputStream os = resp.getOutputStream();
        os.close();
    }
}
