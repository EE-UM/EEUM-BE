package com.eeum.global.support.error.exception;

import com.eeum.global.support.error.ErrorType;
import lombok.Getter;

@Getter
public class CoreApiException extends RuntimeException {
    private ErrorType errorType;
    private Object data;
}
