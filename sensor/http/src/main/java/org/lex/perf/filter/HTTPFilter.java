package org.lex.perf.filter;

import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexType;
import org.lex.perf.api.index.InspectionIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 */
public class HTTPFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPFilter.class);

    private String servletName = "HTTP";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            servletName = filterConfig.getServletContext().getServletContextName();
            IndexFactory.registerIndexSeries(servletName, IndexType.INSPECTION);
        } catch (Error error) {
            LOGGER.error("", error);
            throw error;
        } catch (RuntimeException error) {
            LOGGER.error("", error);
            throw error;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            String requestURI = ((HttpServletRequest) request).getRequestURI();
            InspectionIndex index = (InspectionIndex) IndexFactory.getIndex(servletName, requestURI);
            index.bindContext();
            try {
                chain.doFilter(request, response);
            } finally {
                index.unBindContext();
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
