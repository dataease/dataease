package io.dataease.exception;

import io.dataease.result.ResultCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DEException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8170873998824378304L;
    private int code;

    private String msg;

    public DEException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    private DEException(String message) {
        this(ResultCode.SYSTEM_INNER_ERROR.code(), message);
    }

    private DEException(Throwable t) {
        super(t);
        this.code = ResultCode.SYSTEM_INNER_ERROR.code();
        this.msg = t.getMessage();
    }

    public static String getSafeMessage(Throwable throwable) {
        Throwable rootCause = ExceptionUtils.getRootCause(throwable);
        if (rootCause == null) {
            rootCause = throwable;
        }
        // Preserve wrapped business messages without exposing technical causes.
        return rootCause instanceof DEException ? rootCause.getMessage() : ResultCode.SYSTEM_INNER_ERROR.message();
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
