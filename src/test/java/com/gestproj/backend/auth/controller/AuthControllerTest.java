package com.gestproj.backend.auth.controller;

import com.gestproj.backend.auth.dto.AuthResponse;
import com.gestproj.backend.auth.dto.LoginRequest;
import com.gestproj.backend.auth.dto.RegisterRequest;
import com.gestproj.backend.auth.service.AuthService;
import com.gestproj.backend.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    @Test
    void registerShouldReturnCreatedResponse() {
        when(authService.register(any(RegisterRequest.class))).thenReturn(
                new AuthResponse("token-123", new UserResponse(1L, "user@example.com", "user", "User Name", null))
        );

        var response = authController.register(new RegisterRequest("user@example.com", "user", "password123", "User Name"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("token-123", response.getBody().token());
    }

    @Test
    void loginShouldReturnOkResponse() {
        when(authService.login(any(LoginRequest.class))).thenReturn(
                new AuthResponse("token-456", new UserResponse(1L, "user@example.com", "user", "User Name", null))
        );

        var response = authController.login(new LoginRequest("user@example.com", "password123"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token-456", response.getBody().token());
    }
}
