package org.lex.perf.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: lexas
 * Date: 04.04.14
 * Time: 21:20
 * To change this template use File | Settings | File Templates.
 */
public interface HttpItem {
    void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException;
}
