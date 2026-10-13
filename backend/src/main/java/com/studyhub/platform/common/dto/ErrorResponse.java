package com.studyhub.platform.common.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        boolean success,
        String error,
        String message,
        Map<String, String> validationErrors,
        Instant timestamp
) {
    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(false, error, message, null, Instant.now());
    }

    public static ErrorResponse of(String error, String message, Map<String, String> validationErrors) {
        return new ErrorResponse(false, error, message, validationErrors, Instant.now());
    }
}
