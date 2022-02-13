package com.koolie.bot.richter.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ThreadUtil {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static final ExecutorService threadExecutor = Executors.newCachedThreadPool();
    private ThreadUtil() {
    }

    public static ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public static ExecutorService getThreadExecutor() {
        return threadExecutor;
    }

    public static void shutDownAll() {
        scheduler.shutdown();
        threadExecutor.shutdown();
    }
}
