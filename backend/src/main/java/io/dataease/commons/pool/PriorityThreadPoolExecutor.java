package io.dataease.commons.pool;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import io.dataease.commons.utils.LogUtil;

public class PriorityThreadPoolExecutor extends ThreadPoolExecutor {

    public static AtomicInteger globalInteger = new AtomicInteger(1);

    private ThreadLocal<Integer> local = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 1;
        }
    };

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, getWorkQueue());
    }

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, getWorkQueue(), threadFactory);
    }

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, getWorkQueue(), handler);
    }

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, getWorkQueue(), threadFactory, handler);
    }

    protected static PriorityBlockingQueue getWorkQueue() {
        return new PriorityBlockingQueue();
    }

    @Override
    public void execute(Runnable command) {
        int andIncrement = globalInteger.getAndIncrement();
        Integer theadInteger = local.get();
        try {
            if (theadInteger == 0) {
                this.execute(command, 0);
            } else {
                this.execute(command, andIncrement);
            }

        } finally {
            local.set(1);
        }
    }

    public void execute(Runnable command, int priority) {
        super.execute(new PriorityRunnable(command, priority));
    }

    public <T> Future<T> submit(Callable<T> task, int priority) {
        local.set(priority);
        return super.submit(task);
    }

    protected static class PriorityRunnable<E extends Comparable<? super E>>
            implements Runnable, Comparable<PriorityRunnable<E>> {
        private final static AtomicLong seq = new AtomicLong();
        private final long seqNum;
        Runnable run;
        private int priority;

        public PriorityRunnable(Runnable run, int priority) {
            seqNum = seq.getAndIncrement();
            this.run = run;
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public Runnable getRun() {
            return run;
        }

        @Override
        public void run() {
            LogUtil.info("number " + priority + " is starting...");
            this.run.run();
        }

        @Override
        public int compareTo(PriorityRunnable<E> other) {
            int res = 0;
            if (this.priority == other.priority) {
                if (other.run != this.run) {// ASC
                    res = (seqNum < other.seqNum ? -1 : 1);
                }
            } else {// DESC
                res = this.priority > other.priority ? 1 : -1;
            }
            return res;
        }
    }

}
