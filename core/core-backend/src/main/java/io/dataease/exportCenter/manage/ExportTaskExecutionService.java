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
import java.util.concurrent.RejectedExecutionException;
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
        try {
            Future<?> future = executor.submit(() -> {
                if (!handle.tryStart()) {
                    // 排队阶段已被原子取消，不执行任务体。
                    return;
                }
                try {
                    task.run();
                    handle.complete(ExportTaskState.SUCCESS);
                } catch (Throwable e) {
                    handle.complete(handle.state == ExportTaskState.CANCELLING ? ExportTaskState.CANCELLED : ExportTaskState.FAILED);
                    logger.warn("Export task {} failed", taskId, e);
                }
            });
            handle.future = future;
            return future;
        } catch (RejectedExecutionException rejected) {
            // 提交被拒绝时同步回收登记句柄，避免 future 为 null 的残留登记。
            handles.remove(taskId);
            throw rejected;
        }
    }

    public ExportTaskHandle get(String taskId) {
        return handles.get(taskId);
    }

    /** 原子判断并取消任务，返回实际取消的是排队任务还是运行中任务。 */
    public CancelResult cancel(String taskId) {
        ExportTaskHandle handle = handles.get(taskId);
        if (handle == null) {
            return CancelResult.NOT_FOUND;
        }
        return handle.cancel();
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

    public enum CancelResult {
        QUEUED_CANCELLED,
        RUNNING_MARKED,
        NOT_FOUND
    }

    public static class ExportTaskHandle {
        public final String taskId;
        public volatile ExportTaskState state;
        public volatile Future<?> future;

        ExportTaskHandle(String taskId, ExportTaskState state) {
            this.taskId = taskId;
            this.state = state;
        }

        /** 排队→运行 的原子切换；若排队阶段已被取消则返回 false，任务体不执行。 */
        synchronized boolean tryStart() {
            if (state == ExportTaskState.QUEUED) {
                state = ExportTaskState.RUNNING;
                return true;
            }
            return false;
        }

        /** 原子判断当前状态并取消，返回实际取消的是排队任务还是运行中任务。 */
        synchronized CancelResult cancel() {
            if (state == ExportTaskState.QUEUED) {
                state = ExportTaskState.CANCELLED;
                if (future != null) {
                    future.cancel(false);
                }
                return CancelResult.QUEUED_CANCELLED;
            }
            if (state == ExportTaskState.RUNNING) {
                state = ExportTaskState.CANCELLING;
                if (future != null) {
                    future.cancel(true);
                }
                return CancelResult.RUNNING_MARKED;
            }
            return CancelResult.NOT_FOUND;
        }

        synchronized void complete(ExportTaskState terminalState) {
            if (state == ExportTaskState.CANCELLING) {
                state = ExportTaskState.CANCELLED;
                return;
            }
            if (state != ExportTaskState.CANCELLED) {
                state = terminalState;
            }
        }
    }
}
