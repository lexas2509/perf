package org.lex.perf.web;

import org.lex.perf.event.MonitoringCategory;
import org.lex.perf.event.MonitoringEvent;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 */
public class HTTPFilter implements Filter {
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
                MonitoringEvent.sendDurationItem(MonitoringCategory.HTTP, ((HttpServletRequest) request).getContextPath(), System.currentTimeMillis(), System.nanoTime() - start);
                MonitoringEvent.sendDurationItem(MonitoringCategory.HTTP, "response", System.currentTimeMillis(), System.nanoTime() - start);

            }

        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
