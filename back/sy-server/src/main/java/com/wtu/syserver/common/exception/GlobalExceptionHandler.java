package com.wtu.syserver.common.exception;

import com.wtu.syserver.common.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = BaseException.class)
    public Result<Object> handleBaseException(BaseException e) {
        return Result.error(e.getCode(), e.getMessageEnum());
    }
}
