package io.dataease.exception;

/**
 * @author jianxing.chen
 */
public class ExcelException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExcelException(String message, Exception e) {
        super(message, e);
    }

    public ExcelException(String message) {
        super(message);
    }

}
