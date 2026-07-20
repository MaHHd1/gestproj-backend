package com.gestproj.backend.projectinvitation.dto;

import java.time.LocalDateTime;

import com.gestproj.backend.common.enums.ProjectInvitationStatus;
import com.gestproj.backend.common.enums.ProjectMemberRole;

public record ProjectInvitationResponse(
    Long id,
    Long projectId,
    Long invitedById,
    String invitedEmail,
    String token,
    ProjectInvitationStatus status,
    LocalDateTime expiresAt,
    ProjectMemberRole proposedRole,
    String roleTitle,
    String roleDescription,
    boolean canViewProject,
    boolean canCreateTask,
    boolean canEditTask,
    boolean canDeleteTask,
    boolean canInviteMember,
    boolean canManageMembers) {}
