package com.gestproj.backend.member.dto;

import com.gestproj.backend.common.enums.ProjectMemberRole;

public record ProjectMemberResponse(Long id, Long projectId, Long userId, ProjectMemberRole role) {
}
