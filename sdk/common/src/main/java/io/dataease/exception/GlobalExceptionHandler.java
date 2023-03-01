package io.dataease.exception;

import io.dataease.result.ResultHolder;
import io.dataease.utils.LogUtil;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public ResultHolder deException(DEException exception) {
        exception.printStackTrace();
        LogUtil.error(exception.getMessage(), exception);
        return ResultHolder.error(exception.getMessage(), exception);
    }
}
