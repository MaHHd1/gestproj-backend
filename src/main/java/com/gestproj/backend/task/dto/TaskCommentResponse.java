package com.gestproj.backend.task.dto;

import java.time.LocalDateTime;

public record TaskCommentResponse(
        Long id,
        Long taskId,
        Long userId,
        String username,
        String userEmail,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
