package uk.ac.ucl.context;

import lombok.Getter;
import lombok.Setter;
import org.apache.jasper.JspC;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.logging.log4j.LogManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import uk.ac.ucl.catalina.conf.Host;
import uk.ac.ucl.classLoader.WebappClassLoader;
import uk.ac.ucl.exception.WebConfigDuplicateException;
import uk.ac.ucl.filter.StandardFilterConfig;
import uk.ac.ucl.servlet.StandardServletConfig;
import uk.ac.ucl.util.Constant;
import uk.ac.ucl.util.monitor.FileChangeMonitor;
import uk.ac.ucl.util.core.ReflectUtil;
import uk.ac.ucl.util.core.StrUtil;
import uk.ac.ucl.util.core.TimeUtil;
import uk.ac.ucl.util.io.ContextXMLUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 *
 * Each web app has its own WebappClassLoader
 *
 * Also, url, servlet name and servlet class name map to each other
 */
@Repository
@Scope("prototype")
@Getter @Setter
public class Context {
    // path means the path to access in url
    // docBase means its absolute path in the project
    private String path;
    private String docBase;
    private File webXMLFile;
    private WebappClassLoader webappClassLoader;
    private ServletContext servletContext;
    private Map<Class<?>, HttpServlet> servletPool;
    private List<String> loadOnStartupServiceClassName;

    private Host host;
    private boolean reloadable;
    private FileChangeMonitor fileChangeMonitor;

    private Map<String, String> url_servletClassName;
    private Map<String, String> url_servletName;
    private Map<String, String> servletClassName_servletName;
    private Map<String, String> servletName_servletClassName;
    private Map<String, Map<String, String>> servletClassName_initPara;

    private Map<String, List<String>> url_filterClassName;
    private Map<String, List<String>> url_filterNames;
    private Map<String, String> filterName_filterClassName;
    private Map<String, String> className_filterName;
    private Map<String, Map<String, String>> filterClassName_initParams;

    private Map<String, Filter> filterPool;
    private List<ServletContextListener> contextListeners;


    public Context(String path, String docBase, Host host, boolean reloadable){
        this.path = path;
        this.docBase = docBase;

        this.host = host;
        this.reloadable = reloadable;
        this.servletContext = new BaseContext(this);
        this.servletPool = new HashMap<>();
        this.loadOnStartupServiceClassName = new ArrayList<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        this.webappClassLoader = new WebappClassLoader(docBase, classLoader);

        this.webXMLFile = new File(docBase, ContextXMLUtil.getWatchedResources());

        this.url_servletClassName = new HashMap<>();
        this.url_servletName = new HashMap<>();
        this.servletClassName_servletName = new HashMap<>();
        this.servletName_servletClassName = new HashMap<>();
        this.servletClassName_initPara = new HashMap<>();

        this.url_filterClassName = new HashMap<>();
        this.url_filterNames = new HashMap<>();
        this.filterName_filterClassName = new HashMap<>();
        this.className_filterName = new HashMap<>();
        this.filterClassName_initParams = new HashMap<>();
        this.filterPool = new HashMap<>();

        this.contextListeners = new ArrayList<>();

        try {
            deploy();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void deploy() throws IOException {
        TimeUtil timeUtil = new TimeUtil();
        LogManager.getLogger().info(
                "Deploying web application directory {}", this.docBase);
        loadListeners();
        init();
        LogManager.getLogger().info("Deployment of web application directory {}" +
                " has finished at {} ms", this.docBase, timeUtil.interval());

        if (reloadable){
            fileChangeMonitor = new FileChangeMonitor(Paths.get(this.getDocBase() + "/"), this);
            new Thread(fileChangeMonitor).start();
        }

        JspC jspc = new JspC();
        new JspRuntimeContext(servletContext, jspc);
    }

    private void init() {
        if (!webXMLFile.exists()) { return; }
        try {
            checkDuplicate();
        } catch (WebConfigDuplicateException e) {
            e.printStackTrace();
            return;
        }
        try {
            Document document = Jsoup.parse(webXMLFile, "utf-8");
            parseServletMapping(document);
            parseParaMapping(document);
            parseFilterMapping(document);
            parseFilterParaMapping(document);
            initFilter();
            parseLoadOnStartup(document);
            loadOnStartup();

            fireEvent("init");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        webappClassLoader.stop();
        fileChangeMonitor.stop();
        destroyServlet();

        fireEvent("destroy");
    }

    public void reload(){
        host.reload(this);
    }

    private void parseServletMapping(Document document) {
        // servlet name and servlet class name
        Elements servletNames = document.select("servlet servlet-name");
        for (Element servletName : servletNames) {
            String name = servletName.text();
            String className =
                    servletName.parent().select("servlet-class").first().text();
            servletName_servletClassName.put(name, className);
            servletClassName_servletName.put(className, name);
        }

        // url and servlet name
        Elements urlElements = document.select("servlet-mapping url-pattern");
        for (Element urlElement : urlElements){
            String urlPattern = urlElement.text();
            String servletName =
                    urlElement.parent().select("servlet-name").first().text();
            url_servletName.put(urlPattern, servletName);
        }

        // url and servlet class name
        Set<String> urls = url_servletName.keySet();
        for (String url : urls) {
            String servletName = url_servletName.get(url);
            String servletClassName
                    = servletName_servletClassName.get(servletName);
            url_servletClassName.put(url, servletClassName);
        }
    }

    private void parseFilterMapping(Document document) {
        // URL_name
        Elements urlElements = document.select("filter-mapping url-pattern");
        for (Element urlElement : urlElements) {
            String urlPattern = urlElement.text();
            String filterName = urlElement.parent().select("filter-name").first().text();

            List<String> filterNames = url_filterNames.computeIfAbsent(urlPattern, k -> new ArrayList<>());
            filterNames.add(filterName);
        }

        // className_filterName
        Elements filterNameElements = document.select("filter filter-name");
        for (Element filterNameElement : filterNameElements) {
            String filterName = filterNameElement.text();
            String filterClass = filterNameElement.parent().select("filter-class").first().text();
            filterName_filterClassName.put(filterName, filterClass);
            className_filterName.put(filterClass, filterName);
        }

        // url_filterClassName
        Set<String> urls = url_filterNames.keySet();
        for (String url : urls) {
            List<String> filterNames = url_filterNames.computeIfAbsent(url, k -> new ArrayList<>());
            for (String filterName : filterNames) {
                String filterClassName = filterName_filterClassName.get(filterName);
                List<String> filterClassNames = url_filterClassName.computeIfAbsent(url, k -> new ArrayList<>());
                filterClassNames.add(filterClassName);
            }
        }
    }

    /**
     * Load fliter from their class names, and put into filterPool
     */
    private void initFilter() {
        Set<String> classNames = className_filterName.keySet();
        for (String className : classNames) {
            try {
                Class clazz = this.getWebappClassLoader().loadClass(className);
                Map<String, String> initParams = filterClassName_initParams.get(className);
                String filterName = className_filterName.get(className);
                FilterConfig filterConfig = new StandardFilterConfig(servletContext,
                        initParams, filterName);
                Filter filter = filterPool.get(clazz);
                if (filter == null) {
                    filter = (Filter) ReflectUtil.getInstance(clazz);
                    filter.init(filterConfig);
                    filterPool.put(className, filter);
                }
            } catch (ClassNotFoundException | ServletException e) {
                e.printStackTrace();
            }

        }
    }

    private void parseFilterParaMapping(Document document) {
        Elements classNames = document.select("filter-class");
        for (Element className : classNames) {
            String filterClassName = className.text();
            Elements initParams = className.parents().select("init-param");
            if (initParams.isEmpty()) { continue; }

            Map<String, String> name_value = new HashMap<>();
            for (Element initParam : initParams) {
                String name = initParam.select("param-name").text();
                String value = initParam.select("param-value").text();
                name_value.put(name, value);
            }
            filterClassName_initParams.put(filterClassName, name_value);
        }
    }


    private void parseParaMapping(Document document) {
        Elements servletClassName = document.select("servlet-class");
        for (Element element : servletClassName) {
            String className = element.text();
            Elements initParas = element.parent().select("init-param");
            if (initParas.isEmpty()) { continue; }
            Map<String, String> paraName_paraValue = new HashMap<>();
            for (Element para : initParas) {
                String name = para.select("param-name").get(0).text();
                String value = para.select("param-value").get(0).text();
                paraName_paraValue.put(name, value);
            }
            servletClassName_initPara.put(className, paraName_paraValue);

        }
    }

    /**
     * Check if uri matches the fitler's matching pattern.
     * Three matching rules are implemented:
     * full matching and two kinds of wild card matchings
     * @param pattern
     * @param uri
     * @return
     */
    private boolean match(String pattern, String uri) {
        if (pattern.equals(uri)){
            return true;
        }
        if (pattern.equals("/*")) {
            return true;
        }
        if (pattern.startsWith("/*.")) {
            String ext = StrUtil.subAfter(pattern, ".", true);
            String uriExt = StrUtil.subAfter(uri, ".", true);
            return ext.equals(uriExt);
        }
        return false;
    }

    public List<Filter> getMatchedFilters(String uri) {
        List<Filter> filters = new ArrayList<>();
        Set<String> patterns = url_filterClassName.keySet();
        Set<String> matchedPatterns = new HashSet<>();

        for (String pattern : patterns) {
            if (match(pattern, uri)) {
                matchedPatterns.add(pattern);
            }
        }

        Set<String> matchedFilterClassNames = new HashSet<>();
        for (String matchedPattern : matchedPatterns) {
            List<String> matchedClassNames = url_filterClassName.get(matchedPattern);
            matchedFilterClassNames.addAll(matchedClassNames);
        }

        for (String className : matchedFilterClassNames) {
            Filter filter = filterPool.get(className);
            filters.add(filter);
        }
        return filters;
    }

    private void parseLoadOnStartup(Document document) {
        Elements elements = document.select("load-on-startup");
        for (Element element : elements) {
            int order = Integer.parseInt(element.text());
            String loadClassName = element.parent().select("servlet-class").text();
            if (loadOnStartupServiceClassName.size() > order) {
                loadOnStartupServiceClassName.add(order, loadClassName);
            }
            else{
                loadOnStartupServiceClassName.add(loadClassName);
            }
        }
    }

    private void checkDuplicate(Document document, String pattern, String warning) throws WebConfigDuplicateException {
        Elements elements = document.select(pattern);

        // first put all the elements into a sorted array
        List<String> contents = new ArrayList<>();
        for (Element element : elements){ contents.add(element.text()); }
        Collections.sort(contents);
        // then check if adjacent elements are duplicated
        for (int i = 0; i < contents.size() - 1; i++) {
            if (contents.get(i).equals(contents.get(i + 1))){
                throw new WebConfigDuplicateException(warning);
            }
        }
    }

    private void checkDuplicate() throws WebConfigDuplicateException {
        try {
            Document document = Jsoup.parse(Constant.webXMLFile, "utf-8");
            checkDuplicate(document, "servlet servlet-name",
                    "Duplicate servlet-name");
            checkDuplicate(document, "servlet-mapping url-pattern",
                    "Duplicate url");
            checkDuplicate(document, "servlet servlet-class",
                    "Duplicate servlet-class");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadOnStartup() {
        for (String className : loadOnStartupServiceClassName){
            try {
                // load servlet
                Class<?> clazz = webappClassLoader.loadClass(className);
                // init servlet
                getServlet(clazz);
            } catch (ClassNotFoundException | ServletException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadListeners() {
        if (!webXMLFile.exists()) { return; }
        try {
            Document document = Jsoup.parse(webXMLFile, "utf-8");
            Elements elements = document.select("listener listener-class");
            for (Element element : elements) {
                String listenerClassName = element.text();
                Class<?> listenerClass = this.getWebappClassLoader().loadClass(listenerClassName);
                ServletContextListener contextListener = (ServletContextListener) ReflectUtil.getInstance(listenerClass);
                addListener(contextListener);

            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void fireEvent(String type) {
        ServletContextEvent event = new ServletContextEvent(servletContext);
        for (ServletContextListener servletContextListener : contextListeners) {
            if("init".equals(type))
                servletContextListener.contextInitialized(event);
            if("destroy".equals(type))
                servletContextListener.contextDestroyed(event);
        }
    }

    public WebappClassLoader getWebappClassLoader() { return webappClassLoader; }

    public String getServletClassName(String url){
        String uri = StrUtil.subAfter(url, path);
        if (!uri.startsWith("/")) { uri = "/" + uri; }
        return url_servletClassName.get(uri);
    }

    public HttpServlet getServlet(Class<?> clazz) throws ServletException {
        HttpServlet servlet = servletPool.get(clazz);
        if (servlet == null) {
            servlet = (HttpServlet) ReflectUtil.getInstance(clazz);
            // Init
            ServletContext servletContext = this.getServletContext();
            String className = clazz.getName();
            String servletName = servletClassName_servletName.get(className);
            Map<String, String> initParameters = servletClassName_initPara.get(className);

            ServletConfig servletConfig = new StandardServletConfig(servletContext,
                    initParameters, servletName);
            servlet.init(servletConfig);
            servletPool.put(clazz, servlet);

        }
        return servlet;
    }

    /**
     * Indicating the last stage in servlet life cycle: destroy
     */
    private void destroyServlet() {
        Collection<HttpServlet> servlets = servletPool.values();
        for (HttpServlet servlet : servlets) {
            servlet.destroy();
        }
    }

    public void addListener(ServletContextListener contextListener) {
        this.contextListeners.add(contextListener);
    }
}
