package io.dataease.utils;

import io.dataease.i18n.Translator;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** Bounds actual (including decompressed) bytes and cancels slow continuous responses. */
public final class RemoteTransfer implements AutoCloseable {
    public static final long RESPONSE_BYTES = 16L * 1024 * 1024;
    public static final long FILE_BYTES = 100L * 1024 * 1024;
    public static final long TIMEOUT_MS = 120_000;
    private static final ScheduledThreadPoolExecutor TIMER = new ScheduledThreadPoolExecutor(1, task -> {
        Thread thread = new Thread(task, "remote-transfer-deadline");
        thread.setDaemon(true);
        return thread;
    });
    static { TIMER.setRemoveOnCancelPolicy(true); }

    private final long limit;
    private final long deadline;
    private final Runnable abort;
    private final ScheduledFuture<?> timer;
    private volatile boolean expired;

    public RemoteTransfer(long limit, long timeoutMs, Runnable abort) {
        if (limit <= 0 || timeoutMs <= 0) throw new IllegalArgumentException("Invalid remote transfer limits");
        this.limit = limit;
        this.deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
        this.abort = abort;
        this.timer = TIMER.schedule(() -> { expired = true; Thread.startVirtualThread(abort); }, timeoutMs, TimeUnit.MILLISECONDS);
    }

    public static long sizeBytes(Integer megabytes, int defaultMb, int maximumMb) {
        int value = megabytes == null ? defaultMb : megabytes;
        if (value < 1 || value > maximumMb) {
            throw new IllegalArgumentException(Translator.get("i18n_remote_size_range", maximumMb));
        }
        return value * 1024L * 1024;
    }

    public static long timeoutMillis(Integer seconds) {
        int value = seconds == null ? 120 : seconds;
        if (value < 1 || value > 1800) {
            throw new IllegalArgumentException(Translator.get("i18n_remote_timeout_range"));
        }
        return value * 1000L;
    }

    public void checkLength(long size) throws IOException {
        if (expired || System.nanoTime() >= deadline || Thread.currentThread().isInterrupted()) {
            abort.run();
            throw new IOException(Translator.get("i18n_remote_transfer_timeout"));
        }
        if (size > limit) {
            abort.run();
            throw new IOException(Translator.get("i18n_remote_transfer_size", limit));
        }
    }

    public InputStream wrap(InputStream input) {
        return new FilterInputStream(input) {
            private long count;
            private boolean eof;
            @Override public int read() throws IOException {
                byte[] one = new byte[1];
                return read(one, 0, 1) == -1 ? -1 : one[0] & 0xff;
            }
            @Override public int read(byte[] bytes, int offset, int length) throws IOException {
                if (eof) return -1;
                if (length == 0) return 0;
                checkLength(count);
                int n = in.read(bytes, offset, (int) Math.min(length, limit - count + 1));
                if (n < 0) { eof = true; close(); return -1; }
                count += n;
                checkLength(count);
                return n;
            }
            @Override public long skip(long n) throws IOException {
                long skipped = 0;
                byte[] buffer = new byte[8192];
                while (skipped < n) {
                    int read = read(buffer, 0, (int) Math.min(buffer.length, n - skipped));
                    if (read < 0) break;
                    skipped += read;
                }
                return skipped;
            }
            @Override public boolean markSupported() { return false; }
            @Override public void reset() throws IOException { throw new IOException("Reset unsupported"); }
            @Override public void close() throws IOException {
                // Abort before closing an unfinished entity: Apache must not drain an unbounded body.
                if (!eof) abort.run();
                RemoteTransfer.this.close();
                in.close();
            }
        };
    }

    @Override public void close() { timer.cancel(false); }
}
