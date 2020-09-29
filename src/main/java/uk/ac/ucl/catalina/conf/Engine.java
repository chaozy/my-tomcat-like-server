package uk.ac.ucl.catalina.conf;

import org.springframework.stereotype.Component;
import uk.ac.ucl.util.io.ServerXMLParsing;

import java.util.List;

@Component
public class Engine {
    private String defaultHost;
    private List<Host> hosts;

    public Engine(){
        this.defaultHost = ServerXMLParsing.getEngineDefaultHostName();
        this.hosts = ServerXMLParsing.getHosts(this);
    }

    private void checkDefault(){
        if (getDefaultHost() == null){
            throw new RuntimeException("Default Host " +
                    defaultHost + " does not exists");
        }
    }

    public Host getDefaultHost(){
        for (Host host : hosts){
            if (host.getName().equals(defaultHost)){
                return host;
            }
        }
        return null;
    }
}
