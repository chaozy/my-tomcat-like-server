package uk.ac.ucl.filter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.*;

/**
 * The main job of this class is store Filter's initial parameters
 */
public class StandardFilterConfig implements FilterConfig {
    private ServletContext servletContext;
    private Map<String, String> initParams;
    private String filterName;

    public StandardFilterConfig(ServletContext servletContext,
                                Map<String, String> initParams,
                                String filterName) {
        this.servletContext = servletContext;
        this.initParams = initParams;
        this.filterName = filterName;
        if (initParams == null) {
            this.initParams = new HashMap<>();
        }
    }

    @Override
    public String getFilterName() {
        return this.filterName;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public String getInitParameter(String s) {
        return initParams.get(s);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        Set<String> keys = this.initParams.keySet();
        return Collections.enumeration(keys);
    }
}
