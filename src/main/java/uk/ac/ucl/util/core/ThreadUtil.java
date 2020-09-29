package uk.ac.ucl.util.core;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Create a thread pool to implement multithreading
 */
public class ThreadUtil {
    // Thread pool with 20 core threads, when there are ten tasks waiting in the linked queue
    // the size of pool will increase to 100 with 60 seconds of idle-alive time
    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(20,
            100, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10));

    public static void run(Runnable task){
        pool.execute(task);
    }
}
