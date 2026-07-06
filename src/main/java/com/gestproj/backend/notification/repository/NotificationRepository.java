package com.gestproj.backend.notification.repository;

import com.gestproj.backend.notification.entity.Notification;
import com.gestproj.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUserOrderByCreatedAtDesc(User user);
    List<Notification> findAllByUserAndReadFalseOrderByCreatedAtDesc(User user);
}
