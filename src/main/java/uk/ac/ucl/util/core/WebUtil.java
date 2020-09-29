package uk.ac.ucl.util.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.Map;

/**
 * Including methods used for web connection
 */
public class WebUtil {
    /**
     * Test if the port is used locally via TCP protocol
     * @param port
     * @return
     */
    public static boolean isPortUsable(int port){
        try(ServerSocket ss = new ServerSocket(port);
        ) {
            // The reason to use setReuseAddress() is here:
            // https://stackoverflow.com/questions/23123395/what-is-the-purpose-of-setreuseaddress-in-serversocket
            ss.setReuseAddress(true);
            return false;
        } catch (IOException e) {
            return true;
        }

    }

    public static String urlEncodeUtf8(String s){
        try {
            return URLEncoder.encode(s, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

   public static String toUrlQuery(Map<String, String> params){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) { sb.append("&"); }
            sb.append(String.format("%s=%s",
                    urlEncodeUtf8(entry.getKey()),
                    urlEncodeUtf8(entry.getValue())
            ));
        }
        return sb.toString();
   }
}