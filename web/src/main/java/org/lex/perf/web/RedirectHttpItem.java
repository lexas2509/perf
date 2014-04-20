package org.lex.perf.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
* Created with IntelliJ IDEA.
* User: lexas
* Date: 07.04.14
* Time: 10:15
* To change this template use File | Settings | File Templates.
*/
class RedirectHttpItem implements HttpItem {
    private final String location;

    public RedirectHttpItem(String location) {
        this.location = location;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {

        response.sendRedirect(location);
    }
}
