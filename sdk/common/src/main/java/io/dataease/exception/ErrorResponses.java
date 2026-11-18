package io.dataease.exception;

import io.dataease.result.ResultMessage;
import io.dataease.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;

/** Shared by MVC, servlet filters and authentication endpoints. */
public final class ErrorResponses {
    public static final String REQUEST_ID = "X-DE-Request-ID";
    public static final String CONTRACT = "X-DE-Error-Contract";
    public static final String REASON = "X-DE-Error-Reason";

    private ErrorResponses() { }

    public static boolean modern() {
        HttpServletRequest request = ServletUtils.request();
        return modern(request);
    }

    public static boolean modern(HttpServletRequest request) {
        return request != null && "2".equals(request.getHeader(CONTRACT));
    }

    public static ResultMessage body(DEException exception) {
        return body(exception, ServletUtils.request());
    }

    public static ResultMessage body(DEException exception, HttpServletRequest request) {
        ResultMessage body = new ResultMessage(exception.getCode(), exception.getMessage());
        if (modern(request)) {
            ErrorCategory category = category(exception);
            body.setCategory(category == null ? null : category.name());
            body.setMessageKey(exception.getMessageKey());
            body.setArgs(exception.getArgs());
            body.setData(exception.getErrorData());
            body.setRetryable(category == ErrorCategory.UPSTREAM_TIMEOUT || category == ErrorCategory.UPSTREAM);
            if (request != null) body.setRequestId((String) request.getAttribute(REQUEST_ID));
        }
        return body;
    }

    public static int status(DEException exception) {
        // Old consumers keep the old status of existing codes, even for migrated call sites.
        ErrorCategory category = category(exception);
        if (category != null && modern()) return category.httpStatus();
        return legacyStatus(exception.getCode());
    }

    public static ErrorCategory category(DEException exception) {
        if (exception.getCategory() != null) return exception.getCategory();
        // Reviewed, unambiguous public codes only. 40001 and chart/plugin codes retain legacy behavior.
        return switch (exception.getCode()) {
            case 10001, 10002, 10003, 10004 -> ErrorCategory.VALIDATION;
            case 20001 -> ErrorCategory.UNAUTHENTICATED;
            case 20003, 70001 -> ErrorCategory.FORBIDDEN;
            case 20004, 50001, 70002 -> ErrorCategory.NOT_FOUND;
            case 20005, 50003, 50005, 80001 -> ErrorCategory.CONFLICT;
            case 60002 -> ErrorCategory.UPSTREAM;
            case 60005 -> ErrorCategory.UPSTREAM_TIMEOUT;
            default -> null;
        };
    }

    public static int legacyStatus(int code) {
        return switch (code) {
            case 10001, 10002, 10003, 10004 -> 400;
            case 20001, 20002, 20003, 20004, 20005 -> 401;
            case 30001 -> 422;
            case 50001, 50002, 50003, 50004, 50005 -> 409;
            case 60001, 60002, 60003, 60004, 60005, 60006 -> 502;
            case 70001, 70002 -> 403;
            default -> legacyExtensionStatus(code);
        };
    }

    // Compatibility only for unregistered extension codes; never infer a new category from these ranges.
    private static int legacyExtensionStatus(int code) {
        if (code >= 10001 && code <= 19999) return 400;
        if (code >= 20001 && code <= 29999) return 401;
        if (code >= 30001 && code <= 39999) return 422;
        if (code >= 50001 && code <= 59999) return 409;
        if (code >= 60001 && code <= 69999) return 502;
        if (code >= 70001 && code <= 79999) return 403;
        return 500;
    }
}
