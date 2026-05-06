// global/exception/GlobalExceptionHandler.java
package com.kh.investSpring.global.exception;

import com.kh.investSpring.global.common.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ApiResponse<?> handleCustomException(CustomException e) {
        return ApiResponse.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {
    	e.printStackTrace();
        return ApiResponse.fail("서버 내부 오류");
    }
}