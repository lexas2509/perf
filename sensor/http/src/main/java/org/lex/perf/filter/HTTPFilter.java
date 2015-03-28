package org.lex.perf.filter;

import org.lex.perf.api.factory.IndexFactory;
import org.lex.perf.api.factory.IndexSeries;
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

    IndexSeries indexSeries;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            servletName = filterConfig.getServletContext().getServletContextName();
            indexSeries = IndexFactory.registerIndexSeries(servletName, IndexType.INSPECTION);
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
            indexSeries.bindContext(requestURI);
            try {
                chain.doFilter(request, response);
            } finally {
                indexSeries.unBindContext();
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
