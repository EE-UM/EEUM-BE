package com.eeum.user.config.error;

import com.eeum.common.error.CoreApiException;
import com.eeum.common.error.ErrorType;
import com.eeum.common.response.ApiResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@io.swagger.v3.oas.annotations.Hidden
public class ApiControllerAdvice {
    @ExceptionHandler
    public ResponseEntity<ApiResponse<?>> handleCoreApiException(CoreApiException e) {
        return new ResponseEntity(ApiResponse.error(e.getErrorType(), e.getData()), HttpStatusCode.valueOf(e.getErrorType().getStatusCode()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        return new ResponseEntity(ApiResponse.error(ErrorType.DEFAULT_ERROR, e.getMessage()), HttpStatusCode.valueOf(ErrorType.DEFAULT_ERROR.getStatusCode()));
    }
}
