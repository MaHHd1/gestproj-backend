package com.gestproj.backend.user.dto;

public record UserResponse(
    Long id, String email, String username, String name, String profileImageUrl) {}
