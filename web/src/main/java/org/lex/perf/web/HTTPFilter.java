package org.lex.perf.web;

import org.lex.perf.event.MonitoringCategory;
import org.lex.perf.event.MonitoringEvent;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 */
public class HTTPFilter implements Filter {
    public final static MonitoringCategory HTTP = new MonitoringCategory("HTTP");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            long start = System.nanoTime();
            try {
                chain.doFilter(request, response);
            } finally {
                MonitoringEvent.sendItem(HTTP, ((HttpServletRequest) request).getContextPath(), System.currentTimeMillis(), System.nanoTime() - start);
            }

        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
