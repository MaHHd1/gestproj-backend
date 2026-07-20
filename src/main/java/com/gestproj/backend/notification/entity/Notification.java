package com.gestproj.backend.notification.entity;

import java.time.LocalDateTime;

import com.gestproj.backend.common.enums.NotificationType;
import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.projectinvitation.entity.ProjectInvitation;
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
@Table(name = "notifications")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, columnDefinition = "text")
  private String message;

  @Column(nullable = false)
  private boolean read;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "project_id")
  private Project project;

  @ManyToOne
  @JoinColumn(name = "invitation_id")
  private ProjectInvitation invitation;

  @ManyToOne
  @JoinColumn(name = "project_member_id")
  private ProjectMember projectMember;

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public NotificationType getType() {
    return type;
  }

  public void setType(NotificationType type) {
    this.type = type;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isRead() {
    return read;
  }

  public void setRead(boolean read) {
    this.read = read;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public ProjectInvitation getInvitation() {
    return invitation;
  }

  public void setInvitation(ProjectInvitation invitation) {
    this.invitation = invitation;
  }

  public ProjectMember getProjectMember() {
    return projectMember;
  }

  public void setProjectMember(ProjectMember projectMember) {
    this.projectMember = projectMember;
  }
}
