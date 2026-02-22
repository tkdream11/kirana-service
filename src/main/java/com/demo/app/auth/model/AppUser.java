package com.demo.app.auth.model;

public record AppUser(String email, String passwordHash, String fullName) {
}
