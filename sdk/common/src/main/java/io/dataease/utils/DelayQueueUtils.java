package io.dataease.utils;

import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DelayQueueUtils {

    private static final List<String> delayQueueList = new ArrayList<>();

    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public static void execute(String key, Runnable runnable, Long seconds) {
        seconds = ObjectUtils.isEmpty(seconds) ? 5L : seconds;
        if (delayQueueList.contains(key)) return;
        delayQueueList.add(key);
        executorService.schedule(() -> {
            runnable.run();
            delayQueueList.remove(key);
        }, seconds, TimeUnit.SECONDS);
    }
}
