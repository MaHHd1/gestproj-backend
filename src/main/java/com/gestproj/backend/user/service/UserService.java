package com.gestproj.backend.user.service;

import com.gestproj.backend.common.exception.ConflictException;
import com.gestproj.backend.common.exception.ResourceNotFoundException;
import com.gestproj.backend.user.dto.UserCreateRequest;
import com.gestproj.backend.user.dto.UserResponse;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse create(UserCreateRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        String normalizedUsername = request.username().trim().toLowerCase(Locale.ROOT);

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ConflictException("A user with this email already exists");
        }

        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new ConflictException("A user with this username already exists");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setUsername(normalizedUsername);
        user.setName(request.name().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setProfileImageUrl(null);

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    public UserResponse getById(Long id) {
        return toResponse(findEntityById(id));
    }

    public List<UserResponse> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return userRepository.searchByQuery(query.trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User findEntityByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getUsername(), user.getName(), user.getProfileImageUrl());
    }
}
