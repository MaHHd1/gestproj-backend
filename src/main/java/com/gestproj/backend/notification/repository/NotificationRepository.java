package com.gestproj.backend.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestproj.backend.notification.entity.Notification;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.user.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findAllByUserOrderByCreatedAtDesc(User user);

  List<Notification> findAllByUserAndReadFalseOrderByCreatedAtDesc(User user);

  void deleteAllByProject(Project project);
}
