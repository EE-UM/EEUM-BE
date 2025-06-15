package com.eeum.common.response;

import com.eeum.common.error.ErrorMessage;
import com.eeum.common.error.ErrorType;

public class ApiResponse<T> {
    private ResultType result;
    private T data;
    private ErrorMessage error;

    public ApiResponse(ResultType result, T data, ErrorMessage error) {
        this.result = result;
        this.data = data;
        this.error = error;
    }

    public static ApiResponse<Object> success() {
        return new ApiResponse<>(ResultType.SUCCESS, null, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultType.SUCCESS, data, null);
    }

    public static <T> ApiResponse<T> error(ErrorType errorType, Object errorData) {
        return new ApiResponse<>(ResultType.ERROR, null, new ErrorMessage(errorType, errorData));
    }
}
