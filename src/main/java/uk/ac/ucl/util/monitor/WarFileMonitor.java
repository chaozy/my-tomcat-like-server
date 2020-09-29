package uk.ac.ucl.util.monitor;

import uk.ac.ucl.catalina.conf.Host;
import uk.ac.ucl.util.Constant;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;


public class WarFileMonitor implements Runnable {
    private static final String WAR_INDICATOR = ".war";
    private Host host;

    public WarFileMonitor(Host host) {
        this.host = host;
    }

    private synchronized void start() throws IOException {
        WatchService monitor = FileSystems.getDefault().newWatchService();
        Path targetFile = Constant.rootFolder.toPath();
        targetFile.register(monitor, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        while (true) {
            WatchKey key;
            try {
                key = monitor.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    String fileName = event.context().toString();
                    if (fileName.endsWith(WAR_INDICATOR) && event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                        host.loadWar(new File(Constant.rootFolder, fileName));
                    }
                }
                key.reset();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
