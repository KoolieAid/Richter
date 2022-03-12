package com.koolie.bot.richter.threading;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ThreadUtil {
    private @Getter static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private @Getter static final ExecutorService threadExecutor = Executors.newCachedThreadPool();
    private ThreadUtil() {
    }

    public static void shutDownAll() {
        scheduler.shutdown();
        threadExecutor.shutdown();
    }
}
