package uk.ac.ucl.catalina.conf;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.ac.ucl.context.Context;
import uk.ac.ucl.util.ApplicationContextHolder;
import uk.ac.ucl.util.Constant;
import uk.ac.ucl.util.core.StrUtil;
import uk.ac.ucl.util.io.ServerXMLParsing;
import uk.ac.ucl.util.monitor.WarFileMonitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
@Getter @Setter
public class Host {
    private String name;
    private Map<String, Context> contextMap;
    private Engine engine;

    public Host(String name, Engine engine) {
        this.contextMap = new HashMap<>();
        this.name = name;
        this.engine = engine;

        scanContextRootFolder();
        scanServerXml();
        scanWarInWebApp();

        WarFileMonitor monitor = ApplicationContextHolder.getBean("warFileMonitor", this);
        new Thread(monitor).start();
    }

    /**
     * Scan all of the files and directories under /webapp, creating context for each of them
     * and then put them into a map
     */
    private void scanContextRootFolder() {
        File[] files = Constant.rootFolder.listFiles();
        // Adding the context of the root folder to the context map
        String rootPath = "/";
        String rootDocBase = Constant.rootFolder.getAbsolutePath();
        contextMap.put(rootPath,
                ApplicationContextHolder.getBean("context", rootPath, rootDocBase, this, true));
        // Adding contexts of all directories under root folder to the context map
        for (File file : files){
            String path = "/" + file.getName();
            String docBase;

            if (file.isDirectory()){
                docBase = file.getAbsolutePath();
            }
            else{
                docBase = file.getParentFile().getAbsolutePath();
            }
            Context context =
                    ApplicationContextHolder.getBean("context", path, docBase, this, true);

            contextMap.put(context.getPath(), context);
        }
    }

    /**
     * Scan the /conf/server.xml to find all contexts nodes and put corresponding Context
     * into context map
     */
    private void scanServerXml() {
        List<Context> contextList = ServerXMLParsing.getContexts(this);
        for (Context context : contextList){
            contextMap.put(context.getPath(), context);
        }
    }

    /**
     * Record important information, then delete the old context from contextMap
     * Iniliasing a new context with old basic information, then add it to the contextMap
     * @param context
     */
    public void reload(Context context) {
        LogManager.getLogger().info("Reloading context : [{}] has started", context.getPath());

        String path = context.getPath();
        String docBase = context.getDocBase();
        boolean reloadable = context.isReloadable();

        context.stop();
        contextMap.remove(path);

        Context newContxt = ApplicationContextHolder.getBean("context", path, docBase, this, reloadable);
        contextMap.put(path, newContxt);
    }

    public Context getContext(String path) { return contextMap.get(path); }

    public void load(File folder) {
        String path = "/" + folder.getName();
        String docBase = folder.getAbsolutePath();
        Context context = ApplicationContextHolder.getBean("context", path, docBase, this, false); ;
        contextMap.put(path, context);
    }

    public void loadWar(File warFile) {
        String fileName = warFile.getName();
        String folderName = StrUtil.subBefore(fileName, ".");

        // Check if the context has been established before
        Context context = getContext("/" + folderName);
        if (context != null) { return; }

        // Check if the warFile has been deployed
        File folder = new File(Constant.rootFolder, folderName);
        if (folder.exists()) { return; }

        // Move the war to target dir, as command jar will uncompress the file in local dir
        File tempWarFile = new File(folder, fileName);

        File contextFile = tempWarFile.getParentFile();
        contextFile.mkdir();

        try {
            //FileUtils.copyFile(warFile, tempWarFile);
            Files.copy(warFile.toPath(), tempWarFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // uncompress
        String command = "unzip " + tempWarFile.getAbsolutePath() + " -d " + contextFile.getAbsolutePath();

        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } catch (IOException | InterruptedException e) {

            e.printStackTrace();
        }
        tempWarFile.delete();
        this.load(contextFile);
    }

    private void scanWarInWebApp() {
        File folder = new File(Constant.rootFolder.getAbsolutePath());
        File[] files = folder.listFiles();
        for (File file : files ) {
           if (!file.getName().endsWith(".war")) { continue; }
           loadWar(file);
        }
    }
}
