package com.demo.app.auth.dto;

public record AuthResponse(
        String tokenType,
        String accessToken,
        long accessTokenExpiresInSeconds,
        String refreshToken,
        long refreshTokenExpiresInSeconds
) {
}
