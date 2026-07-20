package com.gestproj.backend.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.gestproj.backend.common.enums.NotificationType;
import com.gestproj.backend.notification.entity.Notification;
import com.gestproj.backend.notification.repository.NotificationRepository;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private NotificationRepository notificationRepository;

  @Mock private UserService userService;

  @InjectMocks private NotificationService notificationService;

  @Test
  void markReadShouldMarkOnlyOwnNotification() {
    User user = new User();
    ReflectionTestUtils.setField(user, "id", 1L);
    user.setEmail("user@example.com");

    Notification notification = new Notification();
    ReflectionTestUtils.setField(notification, "id", 44L);
    notification.setUser(user);
    notification.setType(NotificationType.MEMBER_UPDATED);
    notification.setTitle("Title");
    notification.setMessage("Message");
    notification.setRead(false);

    when(userService.findEntityByEmail("user@example.com")).thenReturn(user);
    when(notificationRepository.findById(44L)).thenReturn(Optional.of(notification));
    when(notificationRepository.save(any(Notification.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response = notificationService.markRead(44L, "user@example.com");

    assertTrue(response.read());
    assertEquals(44L, response.id());
  }

  @Test
  void listUnreadShouldReturnMappedNotifications() {
    User user = new User();
    ReflectionTestUtils.setField(user, "id", 1L);
    user.setEmail("user@example.com");

    Notification notification = new Notification();
    ReflectionTestUtils.setField(notification, "id", 44L);
    notification.setUser(user);
    notification.setType(NotificationType.MEMBER_UPDATED);
    notification.setTitle("Title");
    notification.setMessage("Message");
    notification.setRead(false);

    when(userService.findEntityByEmail("user@example.com")).thenReturn(user);
    when(notificationRepository.findAllByUserAndReadFalseOrderByCreatedAtDesc(user))
        .thenReturn(List.of(notification));

    var unread = notificationService.listUnreadForUser("user@example.com");

    assertEquals(1, unread.size());
    assertEquals(44L, unread.get(0).id());
    assertEquals("Title", unread.get(0).title());
  }
}
