package uk.ac.ucl.util.io;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import uk.ac.ucl.util.Constant;

import java.io.File;
import java.io.IOException;

/**
 * Parsing files
 */
public class HTMLParsing {
    public static String getHead(File file) throws IOException {
        Document document = Jsoup.parse(file, "utf-8");
        Element head = document.head();
        return head.text();
    }

    public static String getBody(File file) throws IOException {
        Document document = Jsoup.parse(file, "utf-8");
        Element body = document.body();
        return body.text();
    }

    public static void main(String[] args) throws IOException {
        String s = getBody(new File(Constant.rootFolder, "hello.html"));
        System.out.println(s);
    }
}
