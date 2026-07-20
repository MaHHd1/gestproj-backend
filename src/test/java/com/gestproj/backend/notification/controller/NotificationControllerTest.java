package com.gestproj.backend.notification.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import com.gestproj.backend.common.enums.NotificationType;
import com.gestproj.backend.notification.dto.NotificationResponse;
import com.gestproj.backend.notification.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

  @Mock private NotificationService notificationService;

  @Mock private Authentication authentication;

  @InjectMocks private NotificationController controller;

  @Test
  void listShouldReturnNotifications() {
    when(authentication.getName()).thenReturn("user@example.com");
    when(notificationService.listForUser("user@example.com"))
        .thenReturn(
            List.of(
                new NotificationResponse(
                    1L,
                    NotificationType.MEMBER_UPDATED,
                    "Title",
                    "Message",
                    false,
                    LocalDateTime.now(),
                    1L,
                    null,
                    null,
                    null)));

    var response = controller.list(authentication);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Title", response.getBody().get(0).title());
  }
}
