import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.ucl.util.MiniBrowser;
import uk.ac.ucl.util.core.StrUtil;
import uk.ac.ucl.util.core.WebUtil;
import uk.ac.ucl.util.io.Zipper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class WebApplicationTest {
    private static int port = 18081;
    private static String ip = "127.0.0.1";
    @BeforeClass
    public static void beforeClass() {
        //所有测试开始前看diy tomcat 是否已经启动了
        if(!WebUtil.isPortUsable(port)) {
            System.err.println("Please start Tomcat at port " + port);
            System.exit(1);
        }
        else {
            System.out.println("Tomcat has been established, unit testing starts");
        }
    }

    private byte[] getContentBytes(String uri, boolean gzip) {
        String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
        return MiniBrowser.getContentBytes(url, gzip);
    }

    private String getContentString(String uri) {
        String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
        String content = MiniBrowser.getContentString(url);
        return content;
    }

    private String getHttpString(String uri) {
        String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
        String http = MiniBrowser.getHttpString(url);
        return http;
    }


    @Test
    public void testServlet() {
        String html = getContentString("/example/hello");
        Assert.assertTrue(html.contains("Hello DIY Tomcat from"));
    }

    @Test
    public void testServletSingelton() {
        String html1 = getContentString("/example/hello");
        String html2 = getContentString("/example/hello");
        Assert.assertEquals(html1, html2);
    }

    @Test
    public void testPostParam() {
        String uri = "/example/param";
        String url = StrUtil.format("http://{}:{}{}", ip, port, uri);
        Map<String, String> map = new HashMap<>();
        map.put("name", "chaozy");
        String html = MiniBrowser.getContentString(url, map, true);
        Assert.assertEquals("Post name --> chaozy", html);
    }

    @Test
    public void testGetParam() {
        String uri = "/example/param";
        String url = StrUtil.format("http://{}:{}{}", ip, port, uri);
        Map<String, String> map = new HashMap<>();
        map.put("name", "chaozy");
        String html = MiniBrowser.getContentString(url, map, false);
        Assert.assertEquals("Get name --> chaozy", html);
    }

    @Test
    public void testHeaderServlet() {
        String uri = "/example/header";
        String html = getContentString(uri);
        // The request user-agent in MiniBrowser is "Chaozy's mini browser / java13"
        Assert.assertEquals("Chaozy's mini browser / java13", html);
    }

    @Test
    public void testsetCookie() {
        String html = getHttpString("/example/setCookie");
        System.out.println(html);
        Assert.assertTrue(html.contains("Set-Cookie: name=Chaozy;Expires="));
    }

    @Test
    public void testgetCookie() throws IOException {
        String url = StrUtil.format("http://{}:{}{}", ip,port,"/example/getCookie");
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestProperty("Cookie","name=Gareen(cookie)");
        conn.connect();
        InputStream is = conn.getInputStream();
        String html = new String(MiniBrowser.readBytes(is, true));
        Assert.assertTrue(html.contains("name:Gareen(cookie)"));
    }

    @Test
    public void testSession() throws IOException {
        String jsessionid = getContentString("/example/setSession");
        if(jsessionid != null) {
            jsessionid = jsessionid.trim();
        }
        String url = StrUtil.format("http://{}:{}{}", ip,
                port,"/example/getSession");
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestProperty("Cookie","JSESSIONID=" + jsessionid);
        conn.connect();
        InputStream is = conn.getInputStream();
        String html = new String(MiniBrowser.readBytes(is, true));
        System.out.println(html);
        Assert.assertTrue(html.contains("Chaozy(session)"));
    }

    @Test
    public void testGzip() {
        byte[] gzipContent = getContentBytes("/example/hello",true);
        byte[] unGzipContent = Zipper.uncompress(gzipContent);
        String html = new String(unGzipContent);
        Assert.assertTrue(html.contains("Hello DIY Tomcat from "));
    }

    @Test
    public void testJsp() {
        String html = getContentString("/example/");
        Assert.assertEquals("hello jsp@example", html);
    }

    @Test
    public void testClientJump() {
        String http_servlet = getHttpString("/example/clientJump");
        Assert.assertTrue(http_servlet.contains("HTTP/1.1 302"));
        String http_jsp = getHttpString("/example/clientJump.jsp");
        Assert.assertTrue(http_servlet.contains("HTTP/1.1 302"));
    }

    @Test
    public void testServerJump(){
        String http_servlet = getHttpString("/example/serverJump");
        Assert.assertTrue(http_servlet.contains("Hello from HelloServlet"));
    }

    @Test
    public void testServerJumpWithAttributes() {
        String http = getHttpString("/example/serverJump");
        Assert.assertTrue(http.contains("name is chaozy"));
    }

    @Test
    public void testWarFileStaticDeployment() {
        String http = getHttpString("/example0/hello");
        Assert.assertTrue(http.contains("Hello DIY Tomcat from HelloServlet from : "));
    }

    @Test
    public void testWarFileDynamicDeployment() {
        String http = getHttpString("/example1/hello");
        Assert.assertTrue(http.contains("Hello DIY Tomcat from HelloServlet from : "));
    }
}
