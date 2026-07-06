package com.gestproj.backend.activitylog.dto;

public record ActivityLogResponse(Long id, Long projectId, Long userId, String action) {
}
