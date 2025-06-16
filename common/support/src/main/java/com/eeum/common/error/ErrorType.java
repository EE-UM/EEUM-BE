package com.eeum.common.error;

import lombok.Getter;

@Getter
public enum ErrorType {

    DEFAULT_ERROR(
            500,
            ErrorCode.INTERNAL_SERVER_ERROR_500,
            "An unexpected error occured."
    ),

    BAD_REQUEST(
            400,
            ErrorCode.CLIENT_REQUEST_ERROR_400,
            "The request is invalid."
    ),

    UNAUTHORIZED(
            401,
            ErrorCode.UNAUTHORIZED_401,
            "Authentication is required to access this resource."
    ),

    FORBIDDEN(
            403,
            ErrorCode.FORBIDDEN_403,
            "You do not have permission to access this resource."
    ),

    NOT_FOUND(
            404,
            ErrorCode.NOT_FOUND_404,
            "The requested resource could not be found."
    ),

    CONFLICT(
            409,
            ErrorCode.CONFLICT_409,
            "A conflict occurred while processing the request."
    ),

    VALIDATION_ERROR(
            422,
            ErrorCode.VALIDATION_ERROR_422,
            "The request contains invalid data or parameters."
    );

    private final int statusCode;
    private final ErrorCode code;
    private final String message;

    ErrorType(int statusCode, ErrorCode code, String message) {
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ErrorCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
