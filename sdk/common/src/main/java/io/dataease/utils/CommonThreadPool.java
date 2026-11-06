package io.dataease.utils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.concurrent.*;

/**
 * @Author gin
 * @Date 2021/4/13 4:08 下午
 */
public class CommonThreadPool {

    private int corePoolSize = 10;

    private int maxQueueSize = 10;

    private int keepAliveSeconds = 600;

    private ThreadPoolExecutor workThreadPool;

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    @PostConstruct
    public void init() {
        // 有界工作池负责执行任务，避免无界队列堆积；调度池只负责定时任务和超时取消。
        workThreadPool = new ThreadPoolExecutor(corePoolSize, corePoolSize, keepAliveSeconds, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(Math.max(1, maxQueueSize)), new ThreadPoolExecutor.AbortPolicy());
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);
        scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
    }

    @PreDestroy
    public void shutdown() {
        if (scheduledThreadPoolExecutor != null) {
            scheduledThreadPoolExecutor.shutdownNow();
        }
        if (workThreadPool != null) {
            workThreadPool.shutdownNow();
        }
    }

    /**
     * 线程池是否可用(工作队列未满)
     *
     * @return true为可用，false不可用
     */
    public boolean available() {
        return workThreadPool.getQueue().size() < maxQueueSize;
    }

    /**
     * 添加任务；工作队列已满时明确抛出繁忙异常，不静默丢弃。
     *
     * @param task 任务
     */
    public void addTask(Runnable task) {
        try {
            workThreadPool.execute(task);
        } catch (RejectedExecutionException e) {
            throw new RejectedExecutionException("CommonThreadPool is busy, queue full: " + maxQueueSize, e);
        }
    }

    /**
     * 添加延迟执行任务
     *
     * @param task  任务
     * @param delay 延迟时间
     * @param unit  延迟时间单位
     */
    public void scheduleTask(Runnable task, long delay, TimeUnit unit) {
        scheduledThreadPoolExecutor.schedule(task, delay, unit);
    }

    /**
     * 添加任务和超时时间（超时时间内未执行完的任务将被终止并移除线程池，防止任务执行时间过长而占用线程池）
     *
     * @param task     任务
     * @param timeOut  超时时间
     * @param timeUnit 超时时间单位
     */
    public void addTask(Runnable task, long timeOut, TimeUnit timeUnit) {
        Future<?> future;
        try {
            future = workThreadPool.submit(() -> {
                try {
                    task.run();
                } catch (Exception exception) {
                    LogUtil.getLogger().error("failed to execute task", exception);
                }
            });
        } catch (RejectedExecutionException e) {
            throw new RejectedExecutionException("CommonThreadPool is busy, queue full: " + maxQueueSize, e);
        }
        // 超时取消由独立调度池触发，不占用工作线程。
        scheduledThreadPoolExecutor.schedule(() -> {
            if (!future.isDone() && future.cancel(true)) {
                LogUtil.getLogger().error("timeout to execute task");
            }
        }, timeOut, timeUnit);
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }
}
