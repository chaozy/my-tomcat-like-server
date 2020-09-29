package uk.ac.ucl.catalina.response;


import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
@Scope("prototype")
@Getter @Setter
public class Response extends BasicResponse {
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private String contentType;
    private byte[] body;
    private int status;
    private List<Cookie> cookies;
    private String redirectPath;

    public Response(){
        // default content-type
        this.contentType = "text/html";
        this.stringWriter = new StringWriter();
        this.printWriter = new PrintWriter(stringWriter);
        this.cookies = new ArrayList<>();
        // When printWriter.print is used, it writes stuff into stringWriter
        // And outputStream will derive the stuff through getBody() then send it to the client
    }

    public byte[] getBody() throws UnsupportedEncodingException {
        if (body == null) {
            String content = stringWriter.toString();
            body = content.getBytes(StandardCharsets.UTF_8);
        }
        return body;
    }

    public String getCookieHeader() {
        String pattern = "EEE, d MMM yyyy HH:mm:ss'GMT'";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
        StringBuffer sb = new StringBuffer();

        for (Cookie cookie : this.cookies) {
            sb.append("\r\n");
            sb.append("Set-Cookie: ");
            sb.append(cookie.getName() + "=" + cookie.getValue() + ";");
            // if cookie does not live forever
            if (cookie.getMaxAge() != -1) {
                sb.append("Expires=");
                Date now = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(now);
                cal.add(Calendar.SECOND, cookie.getMaxAge());
                sb.append(sdf.format(cal.getTime()));
                sb.append(";");
            }
            if (cookie.getPath() != null) {
                sb.append("Path=" + cookie.getPath());
            }
        }
        return sb.toString();
    }

    @Override
    public void addCookie(Cookie cookie) {
        this.cookies.add(cookie);
    }

    @Override
    public PrintWriter getWriter() { return this.printWriter; }

    @Override
    public void sendRedirect(String s) throws IOException {
        this.redirectPath = s;
    }
}
