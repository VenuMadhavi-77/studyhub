package com.studyhub.platform.modules.auth.dto;

import com.studyhub.platform.common.enums.Role;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresInMs,
        Long userId,
        String email,
        String fullName,
        Role role
) {
    public static AuthResponse of(String accessToken, String refreshToken, Long expiresInMs, Long userId, String email, String fullName, Role role) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInMs, userId, email, fullName, role);
    }
}
