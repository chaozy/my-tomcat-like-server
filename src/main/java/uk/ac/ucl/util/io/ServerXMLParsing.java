package uk.ac.ucl.util.io;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import uk.ac.ucl.context.Context;
import uk.ac.ucl.catalina.conf.*;
import uk.ac.ucl.util.ApplicationContextHolder;
import uk.ac.ucl.util.Constant;
import uk.ac.ucl.util.XMLTags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerXMLParsing {
    /**
     * Read context nodes
     * @return
     */
    public static List<Context> getContexts(Host host){
        List<Context> list = new ArrayList<>();
        try {
            Document document = Jsoup.parse(Constant.confServerXML, "utf-8");
            Elements elements = document.select(XMLTags.CONTEXT_TAG);
            for (Element element : elements){
                String path = element.attr(XMLTags.PATH);
                String docBase = element.attr(XMLTags.DOC_BASE);
                boolean reloadable = element.attr(XMLTags.RELOADABLE).equals("true");
                Context context = new Context(path, docBase, host, reloadable);
                list.add(context);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Read default host name
     * @return
     */
    public static String getEngineDefaultHostName(){
        String hostName = null;
        try {
            Document document = Jsoup.parse(Constant.confServerXML, "utf-8");
            Element host = document.select(XMLTags.ENGINE_TAG).first();
            hostName = host.attr(XMLTags.DEFAULT_HOST);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hostName;
    }

    /**
     * Read service name
     * @return
     */
    public static String getServiceName(){
        String serviceName = null;
        try {
            Document document = Jsoup.parse(Constant.confServerXML, "utf-8");
            Element host = document.select(XMLTags.SERVICE_TAG).first();
            serviceName = host.attr(XMLTags.SERVICE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serviceName;
    }

    /**
     * Read all the hosts in the given engine
     * @param engine
     * @return
     */
    public static List<Host> getHosts(Engine engine){
        List<Host> hosts = new ArrayList<>();
        try {
            Document document = Jsoup.parse(Constant.confServerXML, "utf-8");
            Elements elements = document.select(XMLTags.HOST_TAG);
            for (Element element : elements){
                Host host = ApplicationContextHolder.getBean(
                        "host", element.attr(XMLTags.HOST_NAME), engine);
                hosts.add(host);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hosts;
    }

    /**
     * Read all the connectors in a service
     */
    public static List<Connector> getConnectors(Service service) {
        List<Connector> connectors = new ArrayList<>();
        try {
            Document document = Jsoup.parse(Constant.confServerXML, "utf-8");
            Elements elements = document.select(XMLTags.CONNECTOR_TAG);
            for (Element element : elements) {
                int port = Integer.parseInt(element.attr(XMLTags.PORT));
                String compression = element.attr(XMLTags.COMPRESSION);
                int compressionMinSize;
                if (element.attr(XMLTags.COMPRESSION_MIN_SIZE).equals("")){
                    compressionMinSize = 0;
                }
                else{
                    compressionMinSize =
                            Integer.parseInt(element.attr(XMLTags.COMPRESSION_MIN_SIZE));
                }

                String noCompressionUserAgent = element.attr(XMLTags.UNACCEPTED_AGENT);
                String compressionMimeType = element.attr(XMLTags.COMPRESSION_MIME_TYPE);

                Connector connector = ApplicationContextHolder.getBean("connector");
                connector.setCompression(compression);
                connector.setCompressionMimeType(compressionMimeType);
                connector.setNoCompressionUserAgent(noCompressionUserAgent);
                connector.setCompressionMinSize(compressionMinSize);
                connector.setPort(port);
                connectors.add(connector);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connectors;
    }
}

