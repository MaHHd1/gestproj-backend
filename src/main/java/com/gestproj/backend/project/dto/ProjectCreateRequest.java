package com.gestproj.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectCreateRequest(
        @NotBlank @Size(min = 2, max = 100) String name,
        @Size(max = 1000) String description
) {
}
