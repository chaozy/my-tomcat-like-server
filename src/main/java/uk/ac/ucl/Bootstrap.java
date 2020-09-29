package uk.ac.ucl;

import uk.ac.ucl.classLoader.CommonClassLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class aims to launch the server with minimum classes needed,
 * except for the core reflect class are used, only the classloader here are needed
 */
public class Bootstrap {
    private static String initialClassName = "uk.ac.ucl.catalina.conf.Server";
    private static String initialMethodName = "start";

    public static void main(String[] args)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        // There are two ways of starting the server

        // FIRST: directly calling the start() method in class Server
//        Server server = new Server();
//        server.start();

        // SECOND: Loading the Server class through CommonClassLoader,
        // then call the method through reflection
        CommonClassLoader commonClassLoader = new CommonClassLoader();

        // The Context class loader is the class loader that the thread will use to find classes
        Thread.currentThread().setContextClassLoader(commonClassLoader);

        Class<?> serverClass = commonClassLoader.loadClass(initialClassName);
        System.out.println(serverClass.getClassLoader());
        Constructor<?> constructor = serverClass.getConstructor();
        Object serverObject = constructor.newInstance();
        Method m = serverClass.getMethod(initialMethodName);
        m.invoke(serverObject);
    }

}
