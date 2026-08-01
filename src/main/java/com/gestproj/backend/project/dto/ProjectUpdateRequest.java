package com.gestproj.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectUpdateRequest(
    @NotBlank @Size(min = 2, max = 100) String name,
    @Size(max = 1000) String description,
    @Size(max = 255) String repoOwner,
    @Size(max = 255) String repoName) {}
