package com.eeum.global.support.error;

import com.eeum.global.support.error.exception.CoreApiException;
import com.eeum.global.support.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@io.swagger.v3.oas.annotations.Hidden
public class ApiControllerAdvice {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<?>> handleCoreApiException(CoreApiException e) {
        log.info("CoreApiException 발생 - statusCode: {}, errorCode: {}, message: {}, data: {}",
                e.getErrorType().getStatusCode(),
                e.getErrorType().getCode(),
                e.getErrorType().getMessage(),
                e.getData()
        );
        return new ResponseEntity(ApiResponse.error(e.getErrorType(), e.getData()), HttpStatusCode.valueOf(e.getErrorType().getStatusCode()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        return new ResponseEntity(ApiResponse.error(ErrorType.DEFAULT_ERROR, e.getMessage()), HttpStatusCode.valueOf(ErrorType.DEFAULT_ERROR.getStatusCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
        e.getBindingResult().getFieldErrors().forEach(error -> {
            errorMessage.append(String.format("field '%s': %s. ", error.getField(), error.getDefaultMessage()));
        });

        log.debug("Invalid Request: {}", errorMessage);
        return new ResponseEntity<>(ApiResponse.error(ErrorType.VALIDATION_ERROR, e.getMessage()), HttpStatusCode.valueOf(ErrorType.VALIDATION_ERROR.getStatusCode()));
    }
}
