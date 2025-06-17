package com.eeum.common.error.exception;

import com.eeum.common.error.ErrorType;
import lombok.Getter;

@Getter
public class CoreApiException extends RuntimeException {
    private ErrorType errorType;
    private Object data;
}
