package com.gestproj.backend.member.dto;

import com.gestproj.backend.common.enums.ProjectMemberRole;
import com.gestproj.backend.common.enums.ProjectMemberStatus;

public record ProjectMemberResponse(
    Long id,
    Long projectId,
    Long userId,
    String username,
    ProjectMemberRole role,
    ProjectMemberStatus status,
    String roleTitle,
    String roleDescription,
    boolean canViewProject,
    boolean canCreateTask,
    boolean canEditTask,
    boolean canDeleteTask,
    boolean canInviteMember,
    boolean canManageMembers) {}
