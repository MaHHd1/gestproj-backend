package com.gestproj.backend.task.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskCommentCreateRequest(
        @NotBlank(message = "Comment content is required")
        String content
) {
}
