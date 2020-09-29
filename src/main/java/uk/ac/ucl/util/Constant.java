package uk.ac.ucl.util;

import java.io.File;

public class Constant {
    public final static int code_404 = 404;
    public final static int code_200 = 200;
    public final static int code_500 = 500;
    public final static int code_302 = 302;

    public final static String response_head_200 =
            "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: {}{} \r\n\r\n";

    public final static String response_head_200_compression =
            "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: {}{} \r\n" +
                    "Content-Encoding:gzip" +
                    "\r\n\r\n";

    public final static String response_head_302 =
            "HTTP/1.1 302 Found\r\nLocation: {}\r\n\r\n";

    public final static File rootFolder =
            new File(System.getProperty("user.dir"), "src/main/webapp");

    public final static File confFolder =
            new File (System.getProperty("user.dir"), "conf");

    public final static File confServerXML =
            new File(confFolder, "server.xml");

    public final static File webXMLFile =
            new File(confFolder, "web.xml");

    public final static File contextXML =
            new File(confFolder, "context.xml");

    public final static File workFolder = new File(System.getProperty("usr.dir"), "work");

    // TODO: Unix system uses "/" as line separator in directory while Windows uses "\"
    // TODO: https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
    // TODO: Use System.properties to find out which line separator should be used
    // TODO: in order to increse portability


    public final static String response_head_404 = "HTTP/1.1 404 Not Found\r\n" +
            "Content-Type: text/html\r\n\r\n";

    // Derived from Apache Tomcat source code
    public final static String textFormat_404 = "<html><head><title>DIY Tomcat/1.0.1 - Error report</title><style>" +
            "<!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} " +
            "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} " +
            "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} " +
            "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} " +
            "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} " +
            "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}" +
            "A {color : black;}A.name {color : black;}HR {color : #525D76;}--></style> " +
            "</head><body><h1>HTTP Status 404 - {}</h1>" +
            "<HR size='1' noshade='noshade'><p><b>type</b> Status report</p><p><b>message</b> <u>{}</u></p><p><b>description</b> " +
            "<u>The requested resource is not available.</u></p><HR size='1' noshade='noshade'><h3>DiyTocmat 1.0.1</h3>" +
            "</body></html>";

    public static final String response_head_500 = "HTTP/1.1 500 Internal Server Error\r\n"
            + "Content-Type: text/html\r\n\r\n";

    public static final String textFormat_500 = "<html><head><title>DIY Tomcat/1.0.1 - Error report</title><style>"
            + "<!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} "
            + "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} "
            + "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} "
            + "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} "
            + "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} "
            + "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}"
            + "A {color : black;}A.name {color : black;}HR {color : #525D76;}--></style> "
            + "</head><body><h1>HTTP Status 500 - An exception occurred processing {}</h1>"
            + "<HR size='1' noshade='noshade'><p><b>type</b> Exception report</p><p><b>message</b> <u>An exception occurred processing {}</u></p><p><b>description</b> "
            + "<u>The server encountered an internal error that prevented it from fulfilling this request.</u></p>"
            + "<p>Stacktrace:</p>" + "<pre>{}</pre>" + "<HR size='1' noshade='noshade'><h3>DiyTocmat 1.0.1</h3>"
            + "</body></html>";
}
