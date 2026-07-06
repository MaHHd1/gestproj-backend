package com.gestproj.backend.auth.service;

import com.gestproj.backend.auth.dto.LoginRequest;
import com.gestproj.backend.auth.jwt.JwtService;
import com.gestproj.backend.common.exception.UnauthorizedException;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.repository.UserRepository;
import com.gestproj.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldReturnTokenWhenPasswordMatches() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setName("Test User");
        user.setPasswordHash("$2a$10$hash");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "$2a$10$hash")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("token-123");

        var response = authService.login(new LoginRequest("test@example.com", "password123"));

        assertEquals("token-123", response.token());
        assertEquals("test@example.com", response.user().email());
    }

    @Test
    void loginShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () ->
                authService.login(new LoginRequest("missing@example.com", "password123"))
        );
    }
}
