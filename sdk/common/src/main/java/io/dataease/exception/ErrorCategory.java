package io.dataease.exception;

/** HTTP semantics are explicit; existing numeric business codes are not ranges of HTTP statuses. */
public enum ErrorCategory {
    VALIDATION(400, 10001), UNAUTHENTICATED(401, 20001), FORBIDDEN(403, 70001),
    NOT_FOUND(404, 70002), CONFLICT(409, io.dataease.result.ResultCode.VERSION_CONFLICT.code()), UPSTREAM(502, 60002),
    UPSTREAM_TIMEOUT(504, 60005), INTERNAL(500, 40001);

    private final int httpStatus;
    private final int code;

    ErrorCategory(int httpStatus, int code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public int httpStatus() { return httpStatus; }
    public int code() { return code; }
}
