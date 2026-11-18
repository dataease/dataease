package io.dataease.exception;

import io.dataease.result.ResultCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DEException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8170873998824378304L;
    private int code;

    private String msg;

    private ErrorCategory category;
    private String messageKey;
    private Object[] args;
    private Object errorData;

    /** Only pass public message keys and safe interpolation arguments here. */
    public static DEException of(ErrorCategory category, String messageKey, Object... args) {
        DEException exception = new DEException(category.code(), io.dataease.i18n.Translator.get(messageKey, args));
        exception.category = category;
        exception.messageKey = messageKey;
        exception.args = args.clone();
        return exception;
    }

    public DEException causedBy(Throwable cause) {
        initCause(cause);
        return this;
    }

    public DEException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    private DEException(String message) {
        this(ResultCode.SYSTEM_INNER_ERROR.code(), message);
    }

    private DEException(Throwable t) {
        super(t.getMessage(), t);
        this.code = ResultCode.SYSTEM_INNER_ERROR.code();
        this.msg = t.getMessage();
    }

    public static void throwException(String message) {
        throw new DEException(message);
    }

    public static void throwException(int code, String message) {
        throw new DEException(code, message);
    }

    public static DEException getException(String message) {
        throw new DEException(message);
    }

    public static void throwException(Throwable t) {
        throw new DEException(t);
    }
}
