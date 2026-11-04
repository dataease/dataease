package io.dataease.exception;


import io.dataease.i18n.Translator;
import io.dataease.result.ResultCode;
import io.dataease.result.ResultMessage;
import io.dataease.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultMessage> MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        ObjectError objectError = e.getBindingResult().getAllErrors().get(0);
        String msg = objectError.getDefaultMessage();
        msg = Translator.get(msg);
        LogUtil.error(msg);
        return ResponseEntity.badRequest().body(new ResultMessage(ResultCode.PARAM_IS_INVALID.code(), msg));
    }

    @ExceptionHandler(DEException.class)
    public ResponseEntity<ResultMessage> deExceptionHandler(DEException e) {
        LogUtil.error(e.getMessage(), e);
        LogUtil.error(e);
        HttpStatus status = resolveHttpStatus(e.getCode());
        return ResponseEntity.status(status).body(new ResultMessage(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ResultMessage> noUserExceptionHandler(Exception e) {
        String message = e.getMessage();
        LogUtil.error(message, e);
        LogUtil.error(e);
        if (StringUtils.contains(message, "Cannot invoke \"io.dataease.auth.bo.TokenUserBO.getUserId()\" because \"user\" is null")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResultMessage(ResultCode.USER_NOT_LOGGED_IN.code(), ResultCode.USER_NOT_LOGGED_IN.message()));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResultMessage(ResultCode.PARAM_IS_BLANK.code(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultMessage> exceptionHandler(Exception e) {
        LogUtil.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResultMessage(ResultCode.SYSTEM_INNER_ERROR.code(), e.getMessage()));
    }

    private HttpStatus resolveHttpStatus(Integer code) {
        if (code == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (code >= 10001 && code <= 19999) {
            return HttpStatus.BAD_REQUEST;
        }
        if (code >= 20001 && code <= 29999) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (code >= 30001 && code <= 39999) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }
        if (code >= 40001 && code <= 49999) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (code >= 50001 && code <= 59999) {
            return HttpStatus.CONFLICT;
        }
        if (code >= 60001 && code <= 69999) {
            return HttpStatus.BAD_GATEWAY;
        }
        if (code >= 70001 && code <= 79999) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
