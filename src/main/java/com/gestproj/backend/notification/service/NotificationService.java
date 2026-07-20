package com.gestproj.backend.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gestproj.backend.common.enums.NotificationType;
import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.notification.dto.NotificationResponse;
import com.gestproj.backend.notification.entity.Notification;
import com.gestproj.backend.notification.repository.NotificationRepository;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.projectinvitation.entity.ProjectInvitation;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.service.UserService;

@Service
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserService userService;

  public NotificationService(
      NotificationRepository notificationRepository, UserService userService) {
    this.notificationRepository = notificationRepository;
    this.userService = userService;
  }

  public Notification notify(
      User user,
      NotificationType type,
      String title,
      String message,
      Project project,
      ProjectInvitation invitation,
      ProjectMember projectMember) {
    Notification notification = new Notification();
    notification.setUser(user);
    notification.setType(type);
    notification.setTitle(title);
    notification.setMessage(message);
    notification.setRead(false);
    notification.setCreatedAt(LocalDateTime.now());
    notification.setProject(project);
    notification.setInvitation(invitation);
    notification.setProjectMember(projectMember);
    return notificationRepository.save(notification);
  }

  public Notification notifyByEmail(
      String email,
      NotificationType type,
      String title,
      String message,
      Project project,
      ProjectInvitation invitation,
      ProjectMember projectMember) {
    User user = userService.findEntityByEmail(email);
    return notify(user, type, title, message, project, invitation, projectMember);
  }

  public List<NotificationResponse> listForUser(String email) {
    User user = userService.findEntityByEmail(email);
    return notificationRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
        .map(this::toResponse)
        .toList();
  }

  public List<NotificationResponse> listUnreadForUser(String email) {
    User user = userService.findEntityByEmail(email);
    return notificationRepository.findAllByUserAndReadFalseOrderByCreatedAtDesc(user).stream()
        .map(this::toResponse)
        .toList();
  }

  public NotificationResponse markRead(Long id, String email) {
    User user = userService.findEntityByEmail(email);
    Notification notification =
        notificationRepository
            .findById(id)
            .filter(existing -> existing.getUser().getId().equals(user.getId()))
            .orElseThrow(
                () ->
                    new com.gestproj.backend.common.exception.ResourceNotFoundException(
                        "Notification not found"));
    notification.setRead(true);
    return toResponse(notificationRepository.save(notification));
  }

  public void markAllRead(String email) {
    User user = userService.findEntityByEmail(email);
    notificationRepository
        .findAllByUserAndReadFalseOrderByCreatedAtDesc(user)
        .forEach(
            notification -> {
              notification.setRead(true);
              notificationRepository.save(notification);
            });
  }

  private NotificationResponse toResponse(Notification notification) {
    return new NotificationResponse(
        notification.getId(),
        notification.getType(),
        notification.getTitle(),
        notification.getMessage(),
        notification.isRead(),
        notification.getCreatedAt(),
        notification.getProject() == null ? null : notification.getProject().getId(),
        notification.getInvitation() == null ? null : notification.getInvitation().getId(),
        notification.getInvitation() == null ? null : notification.getInvitation().getToken(),
        notification.getProjectMember() == null ? null : notification.getProjectMember().getId());
  }
}
