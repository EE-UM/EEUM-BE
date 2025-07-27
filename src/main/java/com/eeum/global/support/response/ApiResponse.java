package com.eeum.global.support.response;

import com.eeum.global.support.error.ErrorMessage;
import com.eeum.global.support.error.ErrorType;
import lombok.Getter;

@Getter
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
