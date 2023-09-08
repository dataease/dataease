package io.dataease.commons.utils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;

/**
 * @Author gin
 * @Date 2021/4/13 4:08 下午
 */
public class CommonThreadPool {

    private int corePoolSize = 10;

    private int maxQueueSize = 10;

    private int keepAliveSeconds = 600;

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    @PostConstruct
    public void init() {
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(corePoolSize);
        scheduledThreadPoolExecutor.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        if (scheduledThreadPoolExecutor != null) {
            scheduledThreadPoolExecutor.shutdown();
        }
    }

    /**
     * 线程池是否可用(实际队列数是否小于最大队列数)
     *
     * @return true为可用，false不可用
     */
    public boolean available() {
        return scheduledThreadPoolExecutor.getQueue().size() <= maxQueueSize;
    }

    /**
     * 添加任务，不强制限制队列数
     *
     * @param task 任务
     */
    public void addTask(Runnable task) {
        scheduledThreadPoolExecutor.execute(task);
    }

    /**
     * 添加延迟执行任务，不强制限制队列数
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
        scheduledThreadPoolExecutor.execute(() -> {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            try {
                Future future = executorService.submit(task);
                future.get(timeOut, timeUnit); // 此行会阻塞，直到任务执行完或超时
            } catch (TimeoutException timeoutException) {
                LogUtil.getLogger().error("timeout to execute task", timeoutException);
            } catch (Exception exception) {
                LogUtil.getLogger().error("failed to execute task", exception);
            } finally {
                if (!executorService.isShutdown()) {
                    executorService.shutdown();
                }
            }
        });
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
