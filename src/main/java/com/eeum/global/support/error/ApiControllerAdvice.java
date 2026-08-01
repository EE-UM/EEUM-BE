package com.eeum.global.support.error;

import com.eeum.domain.comment.exception.AlreadyFinishedPostException;
import com.eeum.domain.comment.exception.DuplicateMusicException;
import com.eeum.global.support.error.exception.CoreApiException;
import com.eeum.global.support.error.exception.OutboundRateLimitException;
import com.eeum.global.support.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@io.swagger.v3.oas.annotations.Hidden
public class ApiControllerAdvice {

  @ExceptionHandler
  public ResponseEntity<ApiResponse<?>> handleCoreApiException(CoreApiException e) {
    ErrorType errorType = e.getErrorType();
    HttpStatus status = HttpStatus.valueOf(errorType.getStatusCode());

    if (status.is5xxServerError()) {
      log.error("서버 오류 - code: {}, message: {}, data: {}", errorType.getCode(),
          errorType.getMessage(), e.getData());
    }
    if (status.is4xxClientError()) {
      log.warn("클라이언트 오류 - code: {}, message: {}, data: {}", errorType.getCode(),
          errorType.getMessage(), e.getData());
    }

    return toResponse(errorType, e.getData());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<?>> handleMethodNotSupported() {
    log.warn("HttpRequestMethodNotSupportedException - 지원하지 않는 메서드를 사용했습니다.");
    return toResponse(ErrorType.METHOD_NOT_SUPPORTED);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiResponse<?>> handleNoResourceFoundException(
      NoResourceFoundException e, HttpServletRequest request
  ) {
    log.info("NoResourceFoundException - method: {}, uri: {}",
        e.getHttpMethod(), request.getRequestURI());

    return toResponse(ErrorType.NOT_FOUND);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e
  ) {
    String detail = e.getBindingResult().getFieldErrors().stream()
        .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()))
        .collect(Collectors.joining(", "));

    log.info("Validation failed - {}", detail);

    return toResponse(ErrorType.VALIDATION_ERROR, detail);
  }

  @ExceptionHandler(OutboundRateLimitException.class)
  public ResponseEntity<ApiResponse<?>> handleOutboundRateLimitException(
      OutboundRateLimitException e
  ) {

    return toResponse(ErrorType.RETRY_AFTER);
  }

  @ExceptionHandler(AlreadyFinishedPostException.class)
  public ResponseEntity<ApiResponse<?>> handleAlreadyFinishedPostException(
      AlreadyFinishedPostException e
  ) {
    log.info("AlreadyFinishedPostException 발생: {}", e.getMessage());

    return toResponse(ErrorType.ALREADY_FINISHED_POST);
  }

  @ExceptionHandler(DuplicateMusicException.class)
  public ResponseEntity<ApiResponse<?>> handleDuplicateMusicException(
      DuplicateMusicException e
  ) {
    log.info("DuplicateMusicException 발생: {}", e.getMessage());

    return toResponse(ErrorType.DUPLICATED_MUSIC);
  }

  @ExceptionHandler
  public ResponseEntity<ApiResponse<?>> handleException(Exception e, HttpServletRequest request) {
    log.error("Unhandled exception - method: {}, uri: {}",
        request.getMethod(), request.getRequestURI(), e);

    return toResponse(ErrorType.DEFAULT_ERROR);
  }

  private ResponseEntity<ApiResponse<?>> toResponse(ErrorType errorType) {
    return toResponse(errorType, null);
  }

  private ResponseEntity<ApiResponse<?>> toResponse(ErrorType errorType, Object data) {
    return ResponseEntity
        .status(errorType.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON)
        .body(ApiResponse.error(errorType, data));
  }
}
