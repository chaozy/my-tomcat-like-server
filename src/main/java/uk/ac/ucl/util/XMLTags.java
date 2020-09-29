package uk.ac.ucl.util;

/**
 * This class stores the tags that used in xml parsing
 */
public class XMLTags {
    // server.xml
    public final static String HOST_TAG = "Host";
    public final static String SERVICE_TAG = "Service";
    public final static String ENGINE_TAG = "Engine";
    public final static String CONNECTOR_TAG = "Connector";

    public final static String CONTEXT_TAG = "Context";
    public final static String PATH = "path";
    public final static String DOC_BASE = "docBase";
    public final static String RELOADABLE = "reloadable";

    public final static String DEFAULT_HOST = "defaultHost";
    public final static String HOST_NAME = "name";

    public final static String SERVICE_NAME = "name";

    public final static String PORT = "port";
    public final static String COMPRESSION = "compression";
    public final static String COMPRESSION_MIN_SIZE = "compressionMinSize";
    public final static String UNACCEPTED_AGENT = "noCompressionAgent";
    public final static String COMPRESSION_MIME_TYPE = "compressionMimeType";


    // context.xml
    public final static String WATCHED_RESOURCE = "WatchedResource";

    // web.xml
    public final static String MIME_MAPPING = "mime-mapping";
    public final static String EXTENSION = "extension";
    public final static String MIME_TYPE = "mime-type";
    public final static String WELCOME_FILE = "welcome-file";

    public final static String DEFAULT_WELCOME_FILE = "index.html";
    public final static String DEFAULT_MIME_TYPE = "text/html";
}
