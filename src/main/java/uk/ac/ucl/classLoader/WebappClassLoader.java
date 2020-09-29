package uk.ac.ucl.classLoader;

import uk.ac.ucl.util.io.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class WebappClassLoader extends URLClassLoader {
    /**
     *
     * @param docBase the directory to be loaded
     * @param commonClassLoader the parent class loader for delegation
     */
    public WebappClassLoader(String docBase, ClassLoader commonClassLoader) {
        super(new URL[]{}, commonClassLoader);
        try {
            File webinfFolder = new File(docBase, "WEB-INF");
            File classesFolder = new File(webinfFolder, "classes");
            File libFolder = new File(webinfFolder, "lib");
            // classesFolder should be handled as directory so a '/' in the end is needed
            URL url = new URL("file:" + classesFolder.getAbsolutePath() + "/");
            this.addURL(url);

            // If libFolder does not exist (static web resources),
            // it will be ignored by FileUtil.getFiles()
            List<File> fileList = FileUtil.getFiles(libFolder, ".jar");
            for (File file : fileList){
                this.addURL(new URL("file:" + file.getAbsolutePath()));
            }
        }
        catch (MalformedURLException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void stop(){

    }

}
