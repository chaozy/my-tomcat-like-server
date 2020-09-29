package uk.ac.ucl.catalina.conf;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ucl.util.core.TimeUtil;
import uk.ac.ucl.util.io.ServerXMLParsing;

import java.util.List;

@Component
@Getter@Setter
public class Service {
    private String name;

    private Engine engine;
    private Server server;

    private List<Connector> connectors;

    @Autowired
    public Service(Server server, Engine engine){
        this.server = server;
        this.engine = engine;
        this.name = ServerXMLParsing.getServiceName();
        this.connectors = ServerXMLParsing.getConnectors(this);
    }

    public void start() {
        init();
        // Each connector will start to listen for connection signals
        for (Connector connector : connectors) {
            connector.start();
        }
    }

    public void init() {
        TimeUtil timeUtil = new TimeUtil();
        for (Connector connector : connectors) {
            connector.setService(this);
            connector.init(connector.getPort());
        }
        LogManager.getLogger().info("Initialization processed in {} ms",
                timeUtil.interval());
    }
}
