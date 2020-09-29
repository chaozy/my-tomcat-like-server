package uk.ac.ucl.catalina.conf;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.ac.ucl.catalina.request.Request;
import uk.ac.ucl.catalina.response.Response;
import uk.ac.ucl.processor.HttpProcessor;
import uk.ac.ucl.util.ApplicationContextHolder;
import uk.ac.ucl.util.core.ThreadUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Component
@Scope("prototype")
@Setter @Getter
public class Connector implements Runnable {
    private int port;
    private Service service;
    private String compression;
    private int compressionMinSize;
    private String compressionMimeType;
    private String noCompressionUserAgent;

    /**
     * In order to create tomcat-style log
     */
    public void init(int port) {
        System.out.println(this.getClass().getClassLoader());
        //LogManager.getLogger().info("Initializing ProtocolHandler [http-bio-{}]", port);
    }

    public void start() {
        LogManager.getLogger().info("Starting ProtocolHandler [http-bio-{}]", port);
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            System.out.println(port);
            ServerSocket ss = new ServerSocket(port);
            // Start waiting for requests
            while (true) {
                // Listening for a connection to be made to this socket and accepts it.
                Socket socket = ss.accept();
                // Receiving requests in multi-threads
                Runnable task = () -> {
                    try {
                        // Read message from browser
                        Request request = ApplicationContextHolder.getBean(
                                "request", socket, Connector.this);
                        Response response = ApplicationContextHolder.getBean("response");
                        HttpProcessor processor = ApplicationContextHolder.getBean("httpProcessor");
                        processor.execute(socket, request, response);
                    } finally {
                        if (!socket.isClosed()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                ThreadUtil.run(task);
            }
        } catch(IOException e){
            LogManager.getLogger().error(e);
            e.printStackTrace();
        }
    }
}
