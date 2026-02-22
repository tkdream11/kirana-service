package com.example.auth.service;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.model.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final Map<String, AppUser> users = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser register(RegisterRequest request) {
        String email = request.email().toLowerCase().trim();
        if (users.containsKey(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        AppUser user = new AppUser(email, passwordEncoder.encode(request.password()), request.fullName());
        users.put(email, user);
        return user;
    }

    public AppUser authenticate(LoginRequest request) {
        String email = request.email().toLowerCase().trim();
        AppUser user = users.get(email);
        if (user == null || !passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return user;
    }

    public Optional<AppUser> findByEmail(String email) {
        return Optional.ofNullable(users.get(email.toLowerCase().trim()));
    }
}
