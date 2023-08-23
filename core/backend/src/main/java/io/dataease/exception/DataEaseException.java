package io.dataease.exception;

public class DataEaseException extends RuntimeException {

    private DataEaseException(String message) {
        super(message);
    }

    private DataEaseException(Throwable t) {
        super(t);
    }

    public static void throwException(String message) {
        throw new DataEaseException(message);
    }

    public static DataEaseException getException(String message) {
        throw new DataEaseException(message);
    }

    public static void throwException(Throwable t) {
        throw new DataEaseException(t);
    }
}
