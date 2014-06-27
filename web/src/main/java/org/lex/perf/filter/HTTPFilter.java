package org.lex.perf.filter;

import org.lex.perf.common.StandardCategory;
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
                long duration = System.nanoTime() - start;
                MonitoringEvent.sendDurationItem(StandardCategory.HTTP, ((HttpServletRequest) request).getRequestURI(), System.currentTimeMillis(), duration);
                MonitoringEvent.sendDurationItem(StandardCategory.GLOBAL, "http", System.currentTimeMillis(), duration);

            }

        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
