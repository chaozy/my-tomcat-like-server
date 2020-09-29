package uk.ac.ucl.catalina.request;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.ac.ucl.catalina.conf.Connector;
import uk.ac.ucl.context.Context;
import uk.ac.ucl.catalina.conf.Service;
import uk.ac.ucl.util.ApplicationContextHolder;
import uk.ac.ucl.util.MiniBrowser;
import uk.ac.ucl.util.core.ArrayUtil;
import uk.ac.ucl.util.core.StrUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Scope("prototype")
@Getter @Setter
public class Request extends BasicRequest {
    private String requestString;
    private String uri;
    private String method;
    private Socket socket;
    private Context context;
    private Connector connector;
    private boolean forwarded;

    private String queryString;
    private Map<String, String[]> paramMap;
    private Map<String, String> headerMap;
    private Cookie[] cookies;
    private HttpSession session;
    private Map<String, Object> attributesMap;

    private final Logger logger = LogManager.getLogger();

    public Request(Socket socket, Connector connector) throws IOException {
        this.socket = socket;
        this.connector = connector;
        this.paramMap = new HashMap<>();
        this.headerMap = new HashMap<>();
        this.attributesMap = new HashMap<>();

        parseHttpRequest();
        if (StrUtil.isEmpty(requestString)) { return; }
        parseUri();
        parseContext();
        parseMethod();
        parseHeaders(requestString);
        parseCookies();

        logger.info("Request Header: " + requestString);

        if (!"/".equals(context.getPath())) {
            this.uri.substring(context.getPath().length());
            if (this.uri.equals("")) {
                this.uri = "/";
            }
        }

        parseParameters();

    }

    /**
     * You should only use this method when you are sure the parameter has only one value.
     * If the parameter might have more than one value, use getParameterValues.
     * @param s
     * @return
     */
    @Override
    public String getParameter(String s) {
        String[] para = this.paramMap.get(s);
        if (para != null && para.length != 0) {
            return para[0];
        }
        return null;
    }

    private void parseCookies() {
        List<Cookie> cookieList = new ArrayList<>();
        String allCookie = this.headerMap.get("Cookie");
        if (allCookie == null) { return; }
        String[] pairs = allCookie.split(";");
        for (String pair : pairs) {
            String[] value = pair.split("=");
            Cookie cookie = new Cookie(value[0].trim(), value[1].trim());
            cookieList.add(cookie);
        }
        this.cookies = new Cookie[cookieList.size()];
        for (int i = 0; i < cookieList.size(); i++) {
            cookies[i] = cookieList.get(i);
        }
    }

    @Override
    public Enumeration<String> getParameterNames() {
        Set<String> keys = this.paramMap.keySet();
        return Collections.enumeration(keys);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return this.paramMap;
    }

    @Override
    public String[] getParameterValues(String s) {
        return this.paramMap.get(s);
    }

    @Override
    public String getHeader(String s) {
        if (s == null) {
            return null;
        }
        return this.headerMap.get(s);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> keys = this.headerMap.keySet();
        return Collections.enumeration(keys);
    }

    @Override
    public int getIntHeader(String s) {
        String header = this.headerMap.get(s);
        if (header == null){ return -1; }
        try {
            return Integer.parseInt(header);
        }
        catch (NumberFormatException e){
            return 0;
        }
    }

    private void parseHeaders(String requestString) throws IOException {
        StringReader reader = new StringReader(requestString);
        List<String> lines = new ArrayList<>();

        BufferedReader br = new BufferedReader(reader);
        String line = br.readLine();
        while (line != null) {
            lines.add(line);
            line = br.readLine();
        }
        br.close();
        reader.close();
        for (int i = 1; i < lines.size(); i++) {
            line = lines.get(i);
            if (line.length() == 0) { break; }
            String[] name_header = line.split(":");
            this.headerMap.put(name_header[0], name_header[1]);
        }
    }

    /**
     * Take parameters out of the uri
     */
    private void parseParameters(){
        if (this.getMethod().equals("GET")) {
            String url = StrUtil.subBetween(requestString, " ");

            if (url.contains("?")){
                queryString = StrUtil.subAfter(url, "?");
            }

        }
        else if (this.getMethod().equals("POST")) {
            queryString = StrUtil.subAfter(requestString, "\r\n\r\n");
        }
        // if queryString does not have parameters, break this method
        if (queryString == null || queryString.equals(requestString)) {
            return ;
        }

        String[] parameterValues = queryString.split("&");
        if (parameterValues != null) {
            for (String paramterValue : parameterValues) {
                String[] nameValues = paramterValue.split("=");
                String name = nameValues[0];
                String value = nameValues[1];
                String[] values = paramMap.get(name);
                if (values == null) {
                    values = new String[]{value};
                }
                else{
                    values = (String[]) ArrayUtil.append(values, value);
                }
                paramMap.put(name, values);
            }
        }
    }

    private void parseMethod() {
        method = StrUtil.subBefore(requestString, " ");
    }

    private void parseHttpRequest() throws IOException {
        InputStream inputStream = socket.getInputStream();

        // Browser will send keep-alive connection,
        // So unless browser terminates the connection by itself,
        // the server will not receive the terminate signal (-1)
        byte[] bytes = MiniBrowser.readBytes(inputStream, false);
        requestString = new String(bytes, StandardCharsets.UTF_8);

    }

    private void parseUri() {
        String temp;

        temp = StrUtil.subBetween(requestString, " ");
        if (temp.contains("?")) {
            temp = StrUtil.subBefore(temp, "?");
        }
        uri = temp;
    }


    private void parseContext() {
        String path;

        path = StrUtil.subBetween(uri, "/");
        path = "/" + path;
        Service service = this.connector.getService();
        context = service.getEngine().getDefaultHost().getContext(path);
    }

    public ServletContext getServletContext() {
        return context.getServletContext();
    }

    public String getRealPath(String path) {
        return getServletContext().getRealPath(path);
    }

    // Code below from : https://docs.oracle.com/javaee/7/api/javax/servlet/http/HttpServletRequest.html
    @Override
    public String getLocalAddr() {
        return socket.getLocalAddress().getHostAddress();
    }

    @Override
    public String getLocalName() {
        return socket.getLocalAddress().getHostName();
    }

    @Override
    public int getLocalPort() {
        return socket.getLocalPort();
    }

    @Override
    public String getProtocol() {
        return "HTTP:/1.1";
    }

    @Override
    public String getRemoteAddr() {
        InetSocketAddress address = (InetSocketAddress) socket.getRemoteSocketAddress();
        return StrUtil.subAfter(address.getAddress().toString(), "/");
    }

    @Override
    public String getRemoteHost() {
        InetSocketAddress address = (InetSocketAddress) socket.getRemoteSocketAddress();
        return address.getHostName();
    }

    @Override
    public int getRemotePort() {
        return socket.getPort();
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public String getServerName() {
        return getHeader("Host").trim();
    }

    @Override
    public int getServerPort() {
        return getLocalPort();
    }

    @Override
    public String getContextPath() {
        String path = this.context.getPath();
        if (path.equals("/")){
            return "";
        }
        return path;
    }

    @Override
    public String getRequestURI() {
        return uri;
    }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer();
        String scheme = this.getScheme();
        int port = this.getServerPort();

        url.append(scheme);
        url.append(port);
        url.append(this.getServerName());
        if ((scheme.equals("http") && (port != 80)) || (scheme.equals("https") && (port != 443))) {
            url.append(':');
            url.append(port);
        }
        url.append(getRequestURI());
        return url;
    }

    @Override
    public String getServletPath() {
        return uri;
    }

    public String getJsessionIDFromCookie() {
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("JSESSIONID")){
                return cookie.getValue();
            }
        }
        return null;
    }

    public boolean isForwarded() { return forwarded; }

    @Override
    public RequestDispatcher getRequestDispatcher(String uri) {
        return ApplicationContextHolder.getBean("applicationRequestDispatcher", uri);
    }

    @Override
    public void removeAttribute(String name) {
        attributesMap.remove(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributesMap.put(name, value);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Set<String> keys = attributesMap.keySet();
        return Collections.enumeration(keys);
    }

    @Override
    public Object getAttribute(String name) {
        return attributesMap.get(name);
    }
}

