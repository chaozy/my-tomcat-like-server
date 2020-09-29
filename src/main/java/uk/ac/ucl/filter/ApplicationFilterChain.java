package uk.ac.ucl.filter;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;
import java.util.List;

/**
 * This class implements the chain of responsibility design pattern on given filter lists.
 */
@Component
@Scope("prototype")
public class ApplicationFilterChain implements FilterChain {
    private Filter[] filters;
    private Servlet servlet;
    private int pos;

    public ApplicationFilterChain(List<Filter> filters, Servlet servlet) {
        this.filters = new Filter[filters.size()];
        for (int i = 0; i < filters.size(); i++) {
            this.filters[i] = filters.get(i);
        }

        this.servlet = servlet;
    }

    /**
     * Implementing a basic iterator that runs through all the filters
     * and then perform the servlet
     * @param servletRequest
     * @param servletResponse
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
            throws IOException, ServletException {
        if (pos < this.filters.length) {
            Filter filter = this.filters[pos++];
            filter.doFilter(servletRequest, servletResponse, this);
        }
        else{
            servlet.service(servletRequest, servletResponse);
        }
    }
}
