package uk.ac.ucl.classLoader;

import org.apache.logging.log4j.LogManager;
import uk.ac.ucl.context.Context;
import uk.ac.ucl.util.Constant;
import uk.ac.ucl.util.core.StrUtil;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class JspClassLoader extends URLClassLoader {
    private static Map<String, JspClassLoader> map = new HashMap<>();

    public JspClassLoader(URL[] urls) {
        super(urls);
    }

    public static void invaliJspClassLoader(String uri, Context context) {
        String key = context.getPath() + "/" + uri;
        map.remove(key);
    }

    public static synchronized JspClassLoader getJspClassLoader(String uri, Context context) {
        String key = context.getPath() + "/" + uri;
        JspClassLoader loader = map.get(key);
        if (loader == null) {
            loader = new JspClassLoader(context);
            map.put(key, loader);
        }
        return loader;
    }

    private JspClassLoader(Context context) {
        super(new URL[]{}, context.getWebappClassLoader());
        String subFolder;
        String path = context.getPath();
        if (path.equals("/")) {
            subFolder = "_";
        }
        else{
            subFolder = StrUtil.subAfter(path, "/", false);
        }
        File classFolder = new File(Constant.workFolder, subFolder);
        URL url = null;
        try {
            url = new URL("file:" + classFolder.getAbsolutePath() + "/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.addURL(url);

    }
}
