package com.example.auth.security;

import com.example.auth.service.AuthService;
import com.example.auth.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";

    private final JwtService jwtService;
    private final UserService userService;
    private final AuthService authService;

    public JwtAuthFilter(JwtService jwtService, UserService userService, AuthService authService) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String jwt = extractBearerToken(authHeader);

        if (jwt != null) {
            jwtService.extractSubjectFromAccessToken(jwt).ifPresentOrElse(
                    email -> authenticate(email, request),
                    () -> tryAutoRegenerateAccessToken(request, response)
            );
        }

        filterChain.doFilter(request, response);
    }

    private void tryAutoRegenerateAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = request.getHeader(REFRESH_TOKEN_HEADER);
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        String newAccessToken = authService.regenerateAccessToken(refreshToken);
        if (newAccessToken == null) {
            return;
        }

        jwtService.extractSubjectFromAccessToken(newAccessToken)
                .ifPresent(email -> {
                    authenticate(email, request);
                    response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);
                });
    }

    private void authenticate(String email, HttpServletRequest request) {
        userService.findByEmail(email).ifPresent(appUser -> {
            User principal = new User(appUser.email(), appUser.passwordHash(), Collections.emptyList());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        });
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}
