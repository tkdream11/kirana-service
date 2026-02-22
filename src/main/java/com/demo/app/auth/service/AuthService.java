package com.demo.app.auth.service;

import com.demo.app.auth.dto.AuthResponse;
import com.demo.app.auth.dto.LoginRequest;
import com.demo.app.auth.dto.RegisterRequest;
import com.demo.app.auth.model.AppUser;
import com.demo.app.auth.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthService(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        AppUser user = userService.register(request);
        return issueTokens(user.email());
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = userService.authenticate(request);
        return issueTokens(user.email());
    }

    public AuthResponse refresh(String refreshToken) {
        String subject = jwtService.extractSubjectFromRefreshToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        userService.findByEmail(subject)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return issueTokens(subject);
    }

    public String regenerateAccessToken(String refreshToken) {
        String subject = jwtService.extractSubjectFromRefreshToken(refreshToken).orElse(null);
        if (subject == null || userService.findByEmail(subject).isEmpty()) {
            return null;
        }
        return jwtService.generateAccessToken(subject);
    }

    private AuthResponse issueTokens(String email) {
        String accessToken = jwtService.generateAccessToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);
        return new AuthResponse(
                "Bearer",
                accessToken,
                jwtService.accessTokenExpirationInSeconds(),
                refreshToken,
                jwtService.refreshTokenExpirationInSeconds()
        );
    }
}
