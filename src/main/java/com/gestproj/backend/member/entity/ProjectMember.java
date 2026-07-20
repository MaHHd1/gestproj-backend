package com.gestproj.backend.member.entity;

import com.gestproj.backend.common.enums.ProjectMemberRole;
import com.gestproj.backend.common.enums.ProjectMemberStatus;
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

@Entity
@Table(
    name = "project_members",
    uniqueConstraints = {
      @jakarta.persistence.UniqueConstraint(columnNames = {"project_id", "user_id"})
    })
public class ProjectMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProjectMemberRole role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProjectMemberStatus status;

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

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public ProjectMemberRole getRole() {
    return role;
  }

  public void setRole(ProjectMemberRole role) {
    this.role = role;
  }

  public ProjectMemberStatus getStatus() {
    return status;
  }

  public void setStatus(ProjectMemberStatus status) {
    this.status = status;
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
