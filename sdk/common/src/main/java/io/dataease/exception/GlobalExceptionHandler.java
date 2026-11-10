package io.dataease.exception;

import io.dataease.i18n.Translator;
import io.dataease.result.ResultMessage;
import io.dataease.utils.LogUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultMessage> MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        LogUtil.getLogger().warn("Request bean validation failed");
        DEException exception = DEException.of(ErrorCategory.VALIDATION, "i18n_error_invalid_parameter");
        ResultMessage body = ErrorResponses.body(exception);
        if (!e.getBindingResult().getAllErrors().isEmpty()) {
            body.setMsg(Translator.get(java.util.Objects.toString(e.getBindingResult().getAllErrors().get(0).getDefaultMessage(), "i18n_error_invalid_parameter")));
        }
        if (ErrorResponses.modern()) {
            body.setFieldErrors(e.getBindingResult().getAllErrors().stream().map(error ->
                    new ResultMessage.FieldError(error instanceof FieldError field ? field.getField() : "",
                            error.getCode(), Translator.get(java.util.Objects.toString(error.getDefaultMessage(), "i18n_error_invalid_parameter")))).toList());
        }
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, HandlerMethodValidationException.class,
            ConstraintViolationException.class})
    public ResponseEntity<ResultMessage> invalidParameterHandler(Exception e) {
        LogUtil.getLogger().warn("Request parameter binding failed");
        return ResponseEntity.badRequest().body(ErrorResponses.body(
                DEException.of(ErrorCategory.VALIDATION, "i18n_error_invalid_parameter")));
    }

    @ExceptionHandler({org.springframework.web.servlet.resource.NoResourceFoundException.class,
            org.springframework.web.servlet.NoHandlerFoundException.class,
            org.springframework.web.HttpRequestMethodNotSupportedException.class,
            org.springframework.web.HttpMediaTypeNotSupportedException.class,
            org.springframework.web.HttpMediaTypeNotAcceptableException.class})
    public ResponseEntity<ResultMessage> protocolErrorHandler(Exception e) {
        org.springframework.web.ErrorResponse error = (org.springframework.web.ErrorResponse) e;
        DEException failure = DEException.of(error.getStatusCode().value() == 404
                ? ErrorCategory.NOT_FOUND : ErrorCategory.VALIDATION, error.getStatusCode().value() == 404
                ? "i18n_error_not_found" : "i18n_error_invalid_parameter");
        return ResponseEntity.status(error.getStatusCode()).headers(error.getHeaders()).body(ErrorResponses.body(failure));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResultMessage> maxUploadSizeExceededHandler(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(e.getStatusCode()).body(ErrorResponses.body(
                DEException.of(ErrorCategory.VALIDATION, "i18n_upload_size_exceeded")));
    }

    @ExceptionHandler(DEException.class)
    public ResponseEntity<ResultMessage> deExceptionHandler(DEException e) {
        // Exceptions wrapping unknown failures are not safe business messages.
        if (e.getCategory() == null && e.getCause() != null && !(e.getCause() instanceof DEException)) {
            return exceptionHandler(e);
        }
        LogUtil.error("Request failed: code=" + e.getCode(), e);
        return ResponseEntity.status(ErrorResponses.status(e)).body(ErrorResponses.body(e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultMessage> exceptionHandler(Exception e) {
        LogUtil.error("Unhandled request failure", e);
        return ResponseEntity.internalServerError().body(ErrorResponses.body(
                DEException.of(ErrorCategory.INTERNAL, "i18n_error_internal")));
    }
}
