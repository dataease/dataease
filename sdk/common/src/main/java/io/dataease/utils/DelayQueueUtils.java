package io.dataease.utils;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DelayQueueUtils {

    private static final Set<String> pendingKeys = Collections.synchronizedSet(new HashSet<>());

    private static final ScheduledThreadPoolExecutor executorService;

    static {
        ThreadFactory factory = Executors.defaultThreadFactory();
        AtomicInteger threadId = new AtomicInteger();
        // Keep cache operations concurrent without creating a large burst of Redis requests.
        executorService = new ScheduledThreadPoolExecutor(2, task -> {
            Thread thread = factory.newThread(task);
            thread.setName("delay-queue-" + threadId.incrementAndGet());
            return thread;
        });
        executorService.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executorService.setRemoveOnCancelPolicy(true);
    }

    public static void execute(String key, Runnable runnable, Long seconds) {
        if (!pendingKeys.add(key)) return;
        try {
            executorService.schedule(() -> {
                try {
                    runnable.run();
                } catch (Throwable error) {
                    // Keys can contain tokens; do not include them in failure messages.
                    LogUtil.error("Failed to execute delayed task", error);
                    throw error;
                } finally {
                    pendingKeys.remove(key);
                }
            }, seconds == null ? 5L : seconds, TimeUnit.SECONDS);
        } catch (RuntimeException | Error error) {
            pendingKeys.remove(key);
            throw error;
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException error) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            pendingKeys.clear();
        }
    }
}
