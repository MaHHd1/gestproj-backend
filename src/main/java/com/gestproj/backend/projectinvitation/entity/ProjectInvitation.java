package com.gestproj.backend.projectinvitation.entity;

import com.gestproj.backend.common.enums.ProjectInvitationStatus;
import com.gestproj.backend.common.enums.ProjectMemberRole;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_invitations")
public class ProjectInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "invited_by", nullable = false)
    private User invitedBy;

    @Column(name = "invited_email")
    private String invitedEmail;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectInvitationStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "proposed_role", nullable = false)
    private ProjectMemberRole proposedRole;

    @Column(name = "role_title")
    private String roleTitle;

    @Column(name = "role_description", columnDefinition = "text")
    private String roleDescription;

    @Column(name = "can_view_project", nullable = false)
    private boolean canViewProject;

    @Column(name = "can_create_task", nullable = false)
    private boolean canCreateTask;

    @Column(name = "can_edit_task", nullable = false)
    private boolean canEditTask;

    @Column(name = "can_delete_task", nullable = false)
    private boolean canDeleteTask;

    @Column(name = "can_invite_member", nullable = false)
    private boolean canInviteMember;

    @Column(name = "can_manage_members", nullable = false)
    private boolean canManageMembers;

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(User invitedBy) {
        this.invitedBy = invitedBy;
    }

    public String getInvitedEmail() {
        return invitedEmail;
    }

    public void setInvitedEmail(String invitedEmail) {
        this.invitedEmail = invitedEmail;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ProjectInvitationStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectInvitationStatus status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ProjectMemberRole getProposedRole() {
        return proposedRole;
    }

    public void setProposedRole(ProjectMemberRole proposedRole) {
        this.proposedRole = proposedRole;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public void setRoleTitle(String roleTitle) {
        this.roleTitle = roleTitle;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    public boolean isCanViewProject() {
        return canViewProject;
    }

    public void setCanViewProject(boolean canViewProject) {
        this.canViewProject = canViewProject;
    }

    public boolean isCanCreateTask() {
        return canCreateTask;
    }

    public void setCanCreateTask(boolean canCreateTask) {
        this.canCreateTask = canCreateTask;
    }

    public boolean isCanEditTask() {
        return canEditTask;
    }

    public void setCanEditTask(boolean canEditTask) {
        this.canEditTask = canEditTask;
    }

    public boolean isCanDeleteTask() {
        return canDeleteTask;
    }

    public void setCanDeleteTask(boolean canDeleteTask) {
        this.canDeleteTask = canDeleteTask;
    }

    public boolean isCanInviteMember() {
        return canInviteMember;
    }

    public void setCanInviteMember(boolean canInviteMember) {
        this.canInviteMember = canInviteMember;
    }

    public boolean isCanManageMembers() {
        return canManageMembers;
    }

    public void setCanManageMembers(boolean canManageMembers) {
        this.canManageMembers = canManageMembers;
    }
}
