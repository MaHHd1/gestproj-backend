package com.gestproj.backend.activitylog.dto;

import java.time.LocalDateTime;

public record ActivityLogResponse(
        Long id,
        Long projectId,
        Long userId,
        String username,
        String action,
        LocalDateTime createdAt
) {
}
