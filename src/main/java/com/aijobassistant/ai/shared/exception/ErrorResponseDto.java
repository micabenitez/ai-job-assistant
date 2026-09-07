package com.aijobassistant.ai.shared.exception;

import java.time.Instant;

public record ErrorResponseDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ErrorResponseDto of(int status, String error, String message, String path) {
        return new ErrorResponseDto(Instant.now(), status, error, message, path);
    }
}