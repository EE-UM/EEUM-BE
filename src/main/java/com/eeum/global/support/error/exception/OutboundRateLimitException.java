package com.eeum.global.support.error.exception;

public class OutboundRateLimitException extends RuntimeException {

  public OutboundRateLimitException(String message) {
    super(message);
  }
}
