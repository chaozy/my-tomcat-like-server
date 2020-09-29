package uk.ac.ucl.util.monitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.ucl.context.Context;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileChangeMonitor implements Runnable {
    private boolean stop = false;
    private Path path;
    private Context context;
    private Logger logger = LogManager.getLogger();

    public FileChangeMonitor(Path path, Context context) throws IOException {
        this.path = path;
        this.context = context;
    }

    public synchronized void start() throws IOException {
        WatchService monitor = FileSystems.getDefault().newWatchService();
        registerAll(monitor);

        while (true) {
            WatchKey key = null;
            if (stop){ continue; }
            try {
                key = monitor.take();
                for (WatchEvent<?> event : key.pollEvents()){
                    String fileName = event.context().toString();
                    if (fileName.endsWith(".jar") || fileName.endsWith(".class") || fileName.endsWith(".xml")){
                        logger.info(this + " has detected modification about {}", fileName);
                        context.reload();
                    }
                }
                key.reset();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public void stop(){

    }

    private void registerAll(WatchService monitor) throws IOException {
        Files.walkFileTree(this.path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                 dir.register(monitor, StandardWatchEventKinds.ENTRY_CREATE,
                         StandardWatchEventKinds.ENTRY_DELETE,
                         StandardWatchEventKinds.ENTRY_MODIFY);
                 return FileVisitResult.CONTINUE;
            }
        });
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
