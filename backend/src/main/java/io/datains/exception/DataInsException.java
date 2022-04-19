package io.datains.exception;

public class DataInsException extends RuntimeException {

    private DataInsException(String message) {
        super(message);
    }

    private DataInsException(Throwable t) {
        super(t);
    }

    public static void throwException(String message) {
        throw new DataInsException(message);
    }

    public static DataInsException getException(String message) {
        throw new DataInsException(message);
    }

    public static void throwException(Throwable t) {
        throw new DataInsException(t);
    }
}
