package com.gestproj.backend.projectinvitation.dto;

import com.gestproj.backend.common.enums.ProjectMemberRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ProjectInvitationCreateRequest(
    @Email String invitedEmail,
    ProjectMemberRole proposedRole,
    @Size(max = 60) String roleTitle,
    @Size(max = 1000) String roleDescription,
    boolean canViewProject,
    boolean canCreateTask,
    boolean canEditTask,
    boolean canDeleteTask,
    boolean canInviteMember,
    boolean canManageMembers) {}
