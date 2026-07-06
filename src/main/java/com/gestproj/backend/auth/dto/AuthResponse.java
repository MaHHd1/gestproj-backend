package com.gestproj.backend.auth.dto;

import com.gestproj.backend.user.dto.UserResponse;

public record AuthResponse(String token, UserResponse user) {
}
