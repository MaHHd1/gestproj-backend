package com.gestproj.backend.auth.service;

import com.gestproj.backend.auth.dto.AuthResponse;
import com.gestproj.backend.auth.dto.LoginRequest;
import com.gestproj.backend.auth.dto.RegisterRequest;
import com.gestproj.backend.auth.jwt.JwtService;
import com.gestproj.backend.common.exception.UnauthorizedException;
import com.gestproj.backend.user.dto.UserResponse;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.repository.UserRepository;
import com.gestproj.backend.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        UserResponse createdUser = userService.create(new com.gestproj.backend.user.dto.UserCreateRequest(
                request.email(),
                request.username(),
                request.password(),
                request.name()
        ));

        User user = userService.findEntityById(createdUser.id());
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, createdUser);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return new AuthResponse(jwtService.generateToken(user), new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getName(),
                user.getProfileImageUrl()
        ));
    }

    public UserResponse currentUser(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getName(),
                user.getProfileImageUrl()
        );
    }
}
