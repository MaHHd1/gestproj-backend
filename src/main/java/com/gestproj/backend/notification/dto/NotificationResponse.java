package com.gestproj.backend.notification.dto;

import java.time.LocalDateTime;

import com.gestproj.backend.common.enums.NotificationType;

public record NotificationResponse(
    Long id,
    NotificationType type,
    String title,
    String message,
    boolean read,
    LocalDateTime createdAt,
    Long projectId,
    Long invitationId,
    String invitationToken,
    Long projectMemberId) {}
