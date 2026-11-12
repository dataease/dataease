package io.dataease.ai.support;

import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;

public final class AiErrors {
    private AiErrors() { }
    public static final class Failure extends DEException {
        private final String safeCode;
        private long retryAfterMillis;
        private Failure(String code) {
            super("forbidden".equals(code) ? 403 : ("conflict".equals(code) || "task_queue_full".equals(code) || "task_user_queue_full".equals(code)) ? 409 : 500, Translator.get("i18n_ai_" + code));
            safeCode = code;
            if ("task_queue_full".equals(code) || "task_user_queue_full".equals(code)) {
                setCategory(io.dataease.exception.ErrorCategory.CONFLICT); setMessageKey("i18n_ai_" + code);
            }
        }
        public String safeCode() { return safeCode; }
        public long retryAfterMillis() { return retryAfterMillis; }
    }
    public static String code(Exception error) { return error instanceof Failure failure ? failure.safeCode() : "upstream_failed"; }
    public static RuntimeException retryAfter(String code, long millis) { var failure = new Failure(code); failure.retryAfterMillis = Math.max(0, millis); return failure; }
    public static RuntimeException error(String code) {
        return new Failure(code);
    }
}
