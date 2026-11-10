package io.dataease.exception;

import java.net.SocketTimeoutException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.NoRouteToHostException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Classify causes, never translated text. Unknown/local failures remain internal errors. */
public final class RemoteFileErrors {
    private RemoteFileErrors() { }

    public static DEException classify(Throwable failure) {
        for (Throwable cause : causes(failure)) {
            if (cause instanceof DEException de && de.getCategory() != null) return de;
            if (cause instanceof SocketTimeoutException || cause instanceof java.util.concurrent.TimeoutException
                    || cause instanceof org.apache.http.conn.ConnectTimeoutException) {
                return DEException.of(ErrorCategory.UPSTREAM_TIMEOUT, "i18n_error_remote_timeout").causedBy(failure);
            }
            if (cause instanceof ConnectException || cause instanceof UnknownHostException
                    || cause instanceof NoRouteToHostException || cause instanceof javax.net.ssl.SSLException
                    || cause instanceof org.apache.http.NoHttpResponseException
                    || cause instanceof org.apache.http.client.HttpResponseException) {
                return DEException.of(ErrorCategory.UPSTREAM, "i18n_error_remote_failed").causedBy(failure);
            }
            if (cause instanceof IllegalArgumentException || cause instanceof io.dataease.utils.RemoteTransfer.LimitExceededException) {
                return DEException.of(ErrorCategory.VALIDATION, "i18n_error_invalid_parameter").causedBy(failure);
            }
        }
        return DEException.of(ErrorCategory.INTERNAL, "i18n_error_internal").causedBy(failure);
    }

    public static DEException parse(Throwable failure) {
        for (Throwable cause : causes(failure)) {
            if (cause instanceof DEException de && de.getCategory() != null) return de;
            // Explicit file-format failures only; missing classes and program bugs are internal failures.
            if (cause.getClass().getName().equals("org.apache.poi.UnsupportedFileFormatException")
                    || cause.getClass().getName().equals("org.apache.poi.poifs.filesystem.OfficeXmlFileException")
                    || cause instanceof java.util.zip.ZipException
                    || cause instanceof io.dataease.utils.RemoteTransfer.LimitExceededException) {
                return DEException.of(ErrorCategory.VALIDATION, "i18n_error_file_invalid").causedBy(failure);
            }
        }
        return DEException.of(ErrorCategory.INTERNAL, "i18n_error_internal").causedBy(failure);
    }

    private static java.util.List<Throwable> causes(Throwable failure) {
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        java.util.List<Throwable> result = new java.util.ArrayList<>();
        for (Throwable cause = failure; cause != null && seen.add(cause); cause = cause.getCause()) result.add(cause);
        return result;
    }
}
