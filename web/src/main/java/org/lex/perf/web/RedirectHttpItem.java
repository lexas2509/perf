package org.lex.perf.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
*/
public class RedirectHttpItem implements HttpItem {
    private final String location;

    public RedirectHttpItem(String location) {
        this.location = location;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {
        response.sendRedirect(location);
    }
}
