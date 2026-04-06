package com.library.users.common;

import java.time.Instant;
import java.util.Map;

public record ApiResponse<T>(
        String status,
        T data,
        ErrorBody error,
        Map<String, Object> metadata,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data, Map<String, Object> metadata) {
        return new ApiResponse<>("SUCCESS", data, null, metadata, Instant.now());
    }

    public static ApiResponse<Void> error(String code, String message, Map<String, Object> metadata) {
        return new ApiResponse<>("ERROR", null, new ErrorBody(code, message), metadata, Instant.now());
    }

    public record ErrorBody(String code, String message) {}
}
