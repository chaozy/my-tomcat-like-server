package uk.ac.ucl.util.io;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import uk.ac.ucl.util.Constant;
import uk.ac.ucl.util.XMLTags;

import java.io.IOException;


public class ContextXMLUtil {
    public static String getWatchedResources() {
        try {
            Document document = Jsoup.parse(Constant.contextXML, "utf-8");
            Element element = document.selectFirst(XMLTags.WATCHED_RESOURCE);
            return element.text();
        } catch (IOException e) {
            e.printStackTrace();
            // default conf file
            return "WEB-INF/web.xml";
        }
    }
}
