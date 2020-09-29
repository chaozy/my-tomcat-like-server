package uk.ac.ucl.util;

import org.apache.logging.log4j.LogManager;
import uk.ac.ucl.util.core.WebUtil;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * MiniBrowser will simulate the basic http request
 * Used in unit testing
 */
public class MiniBrowser {

    public static byte[] getContentBytes(String url){

        return getContentBytes(url, false, null, false);
    }

    public static String getContentString(String url) {
        return getContentString(url,false, null, false);
    }

    public static String getContentString(String url, Map<String,String> params, boolean isPost){
        return getContentString(url, false, params, isPost);
    }

    public static String getContentString(String url, boolean gzip,
                                          Map<String, String> params, boolean isPost) {

        byte[] result = getContentBytes(url, gzip, params, isPost);
        if(null == result)
            return null;
        try {
            return new String(result,"utf-8").trim();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static byte[] getContentBytes(String url, boolean gzip) {
        return getContentBytes(url, gzip,null,true);
    }

    public static byte[] getContentBytes(String url,
                           boolean gzip, Map<String, String> params, boolean isPost) {
        byte[] response = getHttpBytes(url, gzip, params, isPost);
        byte[] doubleReturn = "\r\n\r\n".getBytes();

        int pos = -1;
        for (int i = 0; i < response.length-doubleReturn.length; i++) {
            byte[] temp = Arrays.copyOfRange(response, i, i + doubleReturn.length);

            if(Arrays.equals(temp, doubleReturn)) {
                pos = i;
                break;
            }
        }
        if(-1==pos)
            return null;

        pos += doubleReturn.length;

        byte[] result = Arrays.copyOfRange(response, pos, response.length);
        return result;
    }

    public static String getHttpString(String url, Map<String, String> params, boolean isPost) {
        byte[] bytes = getHttpBytes(url, false, params, isPost );
        return new String(bytes).trim();
    }

    public static String getHttpString(String url,boolean gzip, Map<String,String> params, boolean isGet) {
        byte[]  bytes=getHttpBytes(url,gzip,params,isGet);
        return new String(bytes).trim();
    }

    public static String getHttpString(String url) {
        return getHttpString(url, false, null, false);
    }

    public static byte[] getHttpBytes(String url, boolean gzip,
                       Map<String, String> params, boolean isPost) {
        byte[] result = null;
        String method = isPost?"POST":"GET";
        try {
            URL u = new URL(url);
            Socket client = new Socket();
            int port = u.getPort();
            if(-1 == port)
                port = 18080;
            InetSocketAddress inetSocketAddress = new InetSocketAddress(u.getHost(), port);
            client.connect(inetSocketAddress, 1000);
            Map<String,String> requestHeaders = new HashMap<>();

            requestHeaders.put("Host", u.getHost()+":"+port);
            requestHeaders.put("Accept", "text/html");
            requestHeaders.put("Connection", "close");
            requestHeaders.put("User-Agent", "Chaozy's mini browser / java13");

            if(gzip) {
                requestHeaders.put("Accept-Encoding", "gzip");
            }
            String path = u.getPath();
            if(path.length()==0) {
                path = "/";
            }
            if (params != null && !isPost){
                String query = WebUtil.toUrlQuery(params);
                path += "?" + query;
            }

            String firstLine = method + " " + path + " HTTP/1.1\r\n";

            StringBuffer httpRequestString = new StringBuffer();
            httpRequestString.append(firstLine);
            Set<String> headers = requestHeaders.keySet();

            for (String header : headers) {
                String headerLine = header + ":" + requestHeaders.get(header)+"\r\n";
                httpRequestString.append(headerLine);
            }

            if(null != params && isPost){

                String paramsString = WebUtil.toUrlQuery(params);
                LogManager.getLogger().info(paramsString);
                httpRequestString.append("\r\n");
                httpRequestString.append(paramsString);
            }
            PrintWriter pWriter = new PrintWriter(client.getOutputStream(), true);
            pWriter.println(httpRequestString);
            InputStream is = client.getInputStream();

            result = readBytes(is, true);
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                result = e.toString().getBytes("utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }

        return result;

    }

    /**
     * Converting an InputStream to bytes
     * @param is
     * @param fully : read all inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readBytes(InputStream is, boolean fully) throws IOException {
        int buffer_size = 1024;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[buffer_size];
        while(true) {
            int length = is.read(buffer);
            // If no byte is available because the stream is at the end of the file,
            // the value -1 is returned
            if(length == -1) { break; }

            baos.write(buffer, 0, length);

            if(!fully && length != buffer_size) { break; }
        }
        return baos.toByteArray();
    }
}
