package uk.ac.ucl.classLoader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class CommonClassLoader extends URLClassLoader {
    public CommonClassLoader() {
        super(new URL[]{});

        File workDir = new File(System.getProperty("user.dir"));
        File libDir = new File(workDir, "lib");
        File[] jarFiles = libDir.listFiles();

        for (File file : jarFiles) {
            if (file.getName().endsWith(".jar")){
                try {
                    URL url = new URL("file:" + file.getAbsolutePath());
                    this.addURL(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
