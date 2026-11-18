import io.dataease.utils.DelayQueueUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Regression {

    private static int passed;

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("interrupted-close")) {
            interruptedClose();
            System.out.println("PASS: interrupted shutdown restores interrupt status");
            return;
        }
        try (var context = new AnnotationConfigApplicationContext(DelayQueueUtils.class)) {
            slowTaskIsolation();
            burstRecovery();
            concurrentDeduplication();
            failureRecovery();
            delayCompatibility();
            shutdown(context);
        }
        System.out.println("PASS: " + passed + " delay queue regression scenarios");
    }

    private static void burstRecovery() throws Exception {
        var started = new CountDownLatch(2);
        var release = new CountDownLatch(1);
        var completed = new CountDownLatch(32);
        var executed = new AtomicInteger();
        try {
            for (int i = 0; i < 2; i++) {
                DelayQueueUtils.execute("occupied-" + i, () -> {
                    started.countDown();
                    awaitRelease(release);
                }, 0L);
            }
            await(started);
            for (int i = 0; i < 32; i++) {
                DelayQueueUtils.execute("burst-" + i, () -> {
                    executed.incrementAndGet();
                    completed.countDown();
                }, 0L);
            }
            check(executed.get() == 0, "executor exceeded its two-worker concurrency limit");
            check(executor().getPoolSize() == 2, "unexpected executor size");
        } finally {
            release.countDown();
        }
        await(completed);
        drained();
        check(executed.get() == 32, "queued tasks were lost or duplicated after recovery");
        passed++;
    }

    private static void interruptedClose() throws Exception {
        var context = new AnnotationConfigApplicationContext(DelayQueueUtils.class);
        DelayQueueUtils.execute("interrupted-close", () -> {
            throw new AssertionError("cancelled task ran");
        }, 60L);
        try {
            Thread.currentThread().interrupt();
            // Spring skips close() when its shutdown lock acquisition is interrupted.
            // Invoke the destroy callback directly to exercise our interruption handling.
            context.getBean(DelayQueueUtils.class).shutdown();
            check(Thread.interrupted(), "shutdown swallowed interrupt status");
            check(executor().awaitTermination(3, TimeUnit.SECONDS), "interrupted close left executor alive");
            check(keys().isEmpty(), "interrupted close retained keys");
        } finally {
            Thread.interrupted();
            new DelayQueueUtils().shutdown();
            context.close();
        }
    }

    private static void slowTaskIsolation() throws Exception {
        var started = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var fast = new CountDownLatch(1);
        try {
            DelayQueueUtils.execute("slow", () -> {
                started.countDown();
                awaitRelease(release);
            }, 0L);
            await(started);
            DelayQueueUtils.execute("fast", fast::countDown, 0L);
            await(fast);
            check(release.getCount() == 1, "fast task waits behind blocked task");
        } finally {
            release.countDown();
        }
        drained();
        passed++;
    }

    private static void concurrentDeduplication() throws Exception {
        var release = new CountDownLatch(1);
        var started = new CountDownLatch(1);
        var calls = new AtomicInteger();
        try (var submitters = Executors.newFixedThreadPool(16)) {
            try {
                for (int i = 0; i < 256; i++) {
                    submitters.submit(() -> DelayQueueUtils.execute("shared", () -> {
                        calls.incrementAndGet();
                        started.countDown();
                        awaitRelease(release);
                    }, 0L));
                }
                submitters.shutdown();
                check(submitters.awaitTermination(3, TimeUnit.SECONDS), "submitters stalled");
                await(started);
                check(calls.get() == 1, "same key executed more than once");
            } finally {
                release.countDown();
            }
        }
        drained();
        var resubmitted = new CountDownLatch(1);
        DelayQueueUtils.execute("shared", resubmitted::countDown, 0L);
        await(resubmitted);
        drained();
        passed++;
    }

    private static void failureRecovery() throws Exception {
        for (boolean fatal : new boolean[]{false, true}) {
            DelayQueueUtils.execute("failure", () -> {
                if (fatal) throw new AssertionError("expected regression failure");
                throw new IllegalStateException("expected regression failure");
            }, 0L);
            drained();
            var recovered = new CountDownLatch(1);
            DelayQueueUtils.execute("failure", recovered::countDown, 0L);
            await(recovered);
            drained();
            passed++;
        }
    }

    private static void delayCompatibility() throws Exception {
        long start = System.nanoTime();
        var second = new AtomicLong();
        var defaultDelay = new AtomicLong();
        var tokenDelay = new AtomicLong();
        var delayed = new CountDownLatch(3);
        DelayQueueUtils.execute("one-second", () -> {
            second.set(System.nanoTime() - start);
            delayed.countDown();
        }, 1L);
        DelayQueueUtils.execute("default", () -> {
            defaultDelay.set(System.nanoTime() - start);
            delayed.countDown();
        }, null);
        DelayQueueUtils.execute("token", () -> {
            tokenDelay.set(System.nanoTime() - start);
            delayed.countDown();
        }, 10L);
        check(delayed.await(15, TimeUnit.SECONDS), "delayed callbacks did not run");
        check(second.get() >= TimeUnit.SECONDS.toNanos(1), "one-second task ran early");
        check(defaultDelay.get() >= TimeUnit.SECONDS.toNanos(5), "default task ran early");
        check(tokenDelay.get() >= TimeUnit.SECONDS.toNanos(10), "token task ran early");
        drained();
        passed++;
    }

    private static void shutdown(AnnotationConfigApplicationContext context) throws Exception {
        var cancelledCalls = new AtomicInteger();
        var started = new CountDownLatch(1);
        var interrupted = new CountDownLatch(1);
        DelayQueueUtils.execute("running-on-close", () -> {
            started.countDown();
            try {
                new CountDownLatch(1).await();
            } catch (InterruptedException expected) {
                Thread.currentThread().interrupt();
                interrupted.countDown();
            }
        }, 0L);
        await(started);
        DelayQueueUtils.execute("pending-on-close", cancelledCalls::incrementAndGet, 60L);
        context.close();
        await(interrupted);
        check(executor().awaitTermination(3, TimeUnit.SECONDS), "running task did not stop");
        check(executor().isTerminated(), "Spring close left executor running");
        check(cancelledCalls.get() == 0, "shutdown ran a task before its delay");
        check(keys().isEmpty(), "shutdown retained pending keys");
        for (int i = 0; i < 2; i++) {
            try {
                DelayQueueUtils.execute("after-close", cancelledCalls::incrementAndGet, 0L);
                throw new AssertionError("submission after close did not fail");
            } catch (RejectedExecutionException expected) {
                check(keys().isEmpty(), "rejected submission retained its key");
            }
        }
        passed++;
    }

    private static void awaitRelease(CountDownLatch latch) {
        try {
            check(latch.await(15, TimeUnit.SECONDS), "blocked callback was not released");
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AssertionError(error);
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            check(latch.await(3, TimeUnit.SECONDS), "timed out waiting for callback");
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new AssertionError(error);
        }
    }

    private static void drained() throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (!keys().isEmpty() && System.nanoTime() < deadline) Thread.sleep(5);
        check(keys().isEmpty(), "completed task retained its key");
    }

    private static Set<?> keys() throws Exception {
        Field field = DelayQueueUtils.class.getDeclaredField("pendingKeys");
        field.setAccessible(true);
        return (Set<?>) field.get(null);
    }

    private static ScheduledThreadPoolExecutor executor() throws Exception {
        Field field = DelayQueueUtils.class.getDeclaredField("executorService");
        field.setAccessible(true);
        return (ScheduledThreadPoolExecutor) field.get(null);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
