package com.eeum.common.error;

import lombok.Getter;

@Getter
public class CoreApiException extends RuntimeException {
    private ErrorType errorType;
    private Object data;
}
