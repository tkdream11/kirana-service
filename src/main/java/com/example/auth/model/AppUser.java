package com.example.auth.model;

public record AppUser(String email, String passwordHash, String fullName) {
}
