package com.eeum.common.error;

import lombok.Getter;

@Getter
public class ErrorMessage {
    private String code;
    private String message;
    private Object data;

    public ErrorMessage(ErrorType errorType, Object data) {
        this.code = errorType.getCode().name();
        this.message = errorType.getMessage();
        this.data = data;
    }
}
