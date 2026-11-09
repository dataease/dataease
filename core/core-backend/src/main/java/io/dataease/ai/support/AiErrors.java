package io.dataease.ai.support;

import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;

public final class AiErrors {
    private AiErrors() { }
    public static final class Failure extends DEException {
        private final String safeCode;
        private Failure(String code) {
            super("forbidden".equals(code) ? 403 : "conflict".equals(code) ? 409 : 500, Translator.get("i18n_ai_" + code));
            safeCode = code;
        }
        public String safeCode() { return safeCode; }
    }
    public static String code(Exception error) { return error instanceof Failure failure ? failure.safeCode() : "upstream_failed"; }
    public static RuntimeException error(String code) {
        return new Failure(code);
    }
}
