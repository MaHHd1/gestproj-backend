package com.gestproj.backend.project.dto;

public record ProjectResponse(
    Long id, String name, String description, Long ownerId, String ownerUsername) {}
