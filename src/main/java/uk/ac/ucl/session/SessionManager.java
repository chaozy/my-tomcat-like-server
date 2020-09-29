package uk.ac.ucl.session;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import uk.ac.ucl.catalina.request.Request;
import uk.ac.ucl.catalina.response.Response;
import uk.ac.ucl.util.Constant;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

public class SessionManager {
    // The map that stores all the sessions
    private static Map<String, StandardSession> sessionMap = new HashMap<>();
    private static int defaultTimeout = getTimeout();

    static {
        startSessionOutdateCheckThread();
    }

    private static int getTimeout(){
        int timeout = 30;
        try {
            Document document = Jsoup.parse(Constant.webXMLFile, "utf-8");
            Elements elements = document.select("session-config session-timeout");
            if (!elements.isEmpty()){
                timeout = Integer.parseInt(elements.get(0).text());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return timeout;
    }

    private static void startSessionOutdateCheckThread(){
        new Thread() {
            public void run() {
                while(true) {
                    checkOutDateSession();
                    try {
                        sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private static void createCookieBySession(HttpSession session, Request request, Response response) {
        Cookie cookie = new Cookie("jsessionID", session.getId());
        cookie.setMaxAge(session.getMaxInactiveInterval());
        cookie.setPath(request.getContext().getPath());
        response.addCookie(cookie);
    }

    private static void checkOutDateSession() {
        Set<String> jsessionids = sessionMap.keySet();
        List<String> timeoutJsessionids = new ArrayList<>();

        for (String jsessionid : jsessionids) {
            StandardSession ss = sessionMap.get(jsessionid);
            long interval = System.currentTimeMillis() - ss.getLastAccessedTime();
            if (interval > ss.getLastAccessedTime() * 1000){
                timeoutJsessionids.add(jsessionid);
            }
        }
        for (String jsessionid : timeoutJsessionids) {
            sessionMap.remove(jsessionid);
        }
    }

    /**
     * This method returns with an ID with length of 4,
     * consisted by both characters and digits.
     * @return
     */
    private static String generateSessionId() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int isNumber = random.nextInt(2);

            if (isNumber == 0) {
                sb.append(random.nextInt(10));
            }
            else{
                sb.append(Character.toChars(random.nextInt(26) + 'a'));
            }
        }
        return sb.toString();
    }

    public int getDefaultTimeout() {
        return defaultTimeout;
    }

    private static HttpSession newSession(Request request, Response response) {
        ServletContext servletContext = request.getServletContext();
        String sid = generateSessionId();
        StandardSession ss = new StandardSession(sid, servletContext);
        ss.setMaxInactiveInterval(defaultTimeout);
        createCookieBySession(ss, request, response);
        sessionMap.put(sid, ss);
        return ss;
    }

    public static HttpSession getSession(String jsessionID,
                                         Request request, Response response) {
        if (jsessionID == null) {
            return newSession(request, response);
        }
        StandardSession ss = sessionMap.get(jsessionID);
        if (ss == null) {
            return newSession(request, response);
        }
        ss.setLastAccessedTime(System.currentTimeMillis());
        createCookieBySession(ss, request, response);
        return ss;
    }

}
