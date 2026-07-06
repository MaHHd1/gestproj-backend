package com.gestproj.backend.notification.dto;

import com.gestproj.backend.common.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        boolean read,
        LocalDateTime createdAt,
        Long projectId,
        Long invitationId,
        Long projectMemberId
) {
}
