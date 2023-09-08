package io.dataease.commons.exception;

public class DEException extends RuntimeException {

    private DEException(String message) {
        super(message);
    }

    private DEException(Throwable t) {
        super(t);
    }

    public static void throwException(String message) {
        throw new DEException(message);
    }

    public static DEException getException(String message) {
        throw new DEException(message);
    }

    public static void throwException(Throwable t) {
        throw new DEException(t);
    }
}
