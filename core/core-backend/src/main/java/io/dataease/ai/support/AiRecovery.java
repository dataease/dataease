package io.dataease.ai.support;

import io.dataease.ai.dao.entity.CoreAiSetting;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** Attempt-scoped recovery: counters/deadline never reset between model or tool steps. */
public final class AiRecovery {
    public record Progress(String stepId, String stepKind, int technicalRetries, int corrections, String reason) { }
    private int stepLimit, attemptLimit, delay;
    private boolean frozen;
    private int retries, corrections, modelSends, toolSends;
    private io.dataease.api.ai.runtime.AiExecutionLink link;
    public synchronized void link(io.dataease.api.ai.runtime.AiExecutionLink link) {
        if (modelSends != 0 || this.link != null) throw AiErrors.error("conflict"); this.link = java.util.Objects.requireNonNull(link);
    }
    public synchronized io.dataease.api.ai.runtime.AiExecutionLink link() { return link; }
    public io.dataease.api.ai.config.AiConfigDtos.Recovery policy() { return new io.dataease.api.ai.config.AiConfigDtos.Recovery(stepLimit, attemptLimit, delay); }
    private Progress progress = new Progress(null, null, 0, 0, null);
    private java.util.function.Consumer<Progress> observer = ignored -> { };
    public AiRecovery(CoreAiSetting settings) {
        stepLimit = settings.getRecoveryStepRetries(); attemptLimit = settings.getRecoveryAttemptRetries(); delay = settings.getRecoveryInitialDelayMillis();
    }
    /** A trusted lifecycle may install the policy captured at acceptance, before any step is published. */
    public synchronized void freeze(io.dataease.api.ai.config.AiConfigDtos.Recovery policy) {
        if (frozen || progress.stepId() != null) throw AiErrors.error("conflict");
        if (policy == null || policy.stepRetries() < 0 || policy.stepRetries() > 5 || policy.attemptRetries() < 0 || policy.attemptRetries() > 5
                || policy.initialDelayMillis() < 100 || policy.initialDelayMillis() > 10000) throw AiErrors.error("configuration_unavailable");
        stepLimit = policy.stepRetries(); attemptLimit = policy.attemptRetries(); delay = policy.initialDelayMillis(); frozen = true;
    }
    public synchronized Progress progress() { return progress; }
    public synchronized void observe(java.util.function.Consumer<Progress> observer) { this.observer = this.observer.andThen(java.util.Objects.requireNonNull(observer)); }
    private synchronized void publish(String step, String kind, String reason) {
        progress = new Progress(step, kind, retries, corrections, reason); observer.accept(progress);
    }
    public synchronized boolean correct(String canonical) {
        if (corrections >= 2) return false;
        corrections++; publish(progress.stepId(), progress.stepKind(), "model_correction"); return true;
    }
    public <T> T execute(String kind, long deadline, Runnable guard, BooleanSupplier delivered, Runnable retrySend, Supplier<T> work) {
        String step = UUID.randomUUID().toString(); int retry = 0;
        publish(step, kind, null);
        while (true) {
            guard.run();
            try {
                synchronized (this) {
                    if ("MODEL".equals(kind) && ++modelSends > AiToolProtocol.MAX_MODEL_STEPS
                            || "TOOL".equals(kind) && ++toolSends > AiToolProtocol.MAX_MODEL_STEPS - 1) throw AiErrors.error("output_limit");
                }
                return work.get();
            }
            catch (RuntimeException failure) {
                String code = failure instanceof AiErrors.Failure known ? known.safeCode() : AiTransport.connectionFailure(failure);
                synchronized (this) {
                    if (delivered.getAsBoolean() || !Set.of("connection_failed", "provider_rate_limit", "provider_unavailable").contains(code)
                            || retry >= stepLimit || retries >= attemptLimit || System.currentTimeMillis() >= deadline) throw failure;
                    retry++; retries++; publish(step, kind, code);
                }
                long vendor = failure instanceof AiErrors.Failure known ? known.retryAfterMillis() : 0;
                long wait = Math.max(vendor, Math.min(30000L, (long) delay << Math.min(retry - 1, 4)) + ThreadLocalRandom.current().nextLong(101));
                long until = Math.min(deadline, System.currentTimeMillis() + wait);
                while (System.currentTimeMillis() < until) {
                    guard.run();
                    try { Thread.sleep(Math.min(50, Math.max(1, until - System.currentTimeMillis()))); }
                    catch (InterruptedException stopped) { Thread.currentThread().interrupt(); throw AiErrors.error("cancelled"); }
                }
                guard.run(); retrySend.run();
            }
        }
    }
}
