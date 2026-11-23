package io.dataease.exportCenter.manage;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 导出任务统一执行服务：提交、登记、查询、移除执行句柄。
 * 取消请求只把状态置为 CANCELLING；任务真正退出后由包装器移除句柄。
 */
@Component
public class ExportTaskExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(ExportTaskExecutionService.class);

    @Value("${dataease.export.core.size:10}")
    private int core;
    @Value("${dataease.export.max.size:10}")
    private int max;
    @Value("${dataease.export.queue.size:100}")
    private int queueSize;
    @Value("${dataease.export.keepalive.seconds:600}")
    private int keepAliveSeconds;

    private ThreadPoolExecutor executor;
    private final ConcurrentHashMap<String, ExportTaskHandle> handles = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        executor = new ThreadPoolExecutor(core, max, keepAliveSeconds, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(Math.max(1, queueSize)), new ThreadPoolExecutor.AbortPolicy());
    }

    public Future<?> submit(String taskId, Runnable task) {
        ExportTaskHandle handle = new ExportTaskHandle(taskId, ExportTaskState.QUEUED);
        handles.put(taskId, handle);
        Future<?> future = executor.submit(() -> {
            handle.state = ExportTaskState.RUNNING;
            try {
                task.run();
                if (handle.state != ExportTaskState.CANCELLING && handle.state != ExportTaskState.CANCELLED) {
                    handle.state = ExportTaskState.SUCCESS;
                }
            } catch (Throwable e) {
                handle.state = handle.state == ExportTaskState.CANCELLING ? ExportTaskState.CANCELLED : ExportTaskState.FAILED;
                logger.warn("Export task {} failed", taskId, e);
            } finally {
                handles.remove(taskId);
            }
        });
        handle.future = future;
        return future;
    }

    public ExportTaskHandle get(String taskId) {
        return handles.get(taskId);
    }

    public boolean cancel(String taskId) {
        ExportTaskHandle handle = handles.get(taskId);
        if (handle == null) {
            return false;
        }
        if (handle.state == ExportTaskState.QUEUED) {
            handle.state = ExportTaskState.CANCELLED;
            handles.remove(taskId);
            return handle.future == null || handle.future.cancel(false);
        }
        if (handle.state == ExportTaskState.RUNNING) {
            handle.state = ExportTaskState.CANCELLING;
            return handle.future.cancel(true);
        }
        return false;
    }

    /** 返回已结束并被移除的 taskId，供上层推送状态与清理。 */
    public List<String> drainCompleted() {
        List<String> done = new ArrayList<>();
        handles.forEach((taskId, handle) -> {
            if (handle.future != null && handle.future.isDone()) {
                handles.remove(taskId);
                done.add(taskId);
            }
        });
        return done;
    }

    public int activeCount() {
        return handles.size();
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000)
    public void logMetrics() {
        logger.info("Export task execution metrics: active={}", handles.size());
    }

    public static class ExportTaskHandle {
        public final String taskId;
        public volatile ExportTaskState state;
        public volatile Future<?> future;

        ExportTaskHandle(String taskId, ExportTaskState state) {
            this.taskId = taskId;
            this.state = state;
        }
    }
}
