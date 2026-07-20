package com.gestproj.backend.common.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.projectinvitation.entity.ProjectInvitation;
import com.gestproj.backend.user.entity.User;

class EmailServiceTest {

  @Test
  void sendProjectInvitationEmailShouldSkipWhenMailDisabled() {
    JavaMailSender mailSender = mock(JavaMailSender.class);
    EmailService emailService =
        new EmailService(mailSender, false, "no-reply@example.com", "http://localhost:3000");

    emailService.sendProjectInvitationEmail(invitation());

    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendProjectInvitationEmailShouldSendInvitationLinkWhenEnabled() {
    JavaMailSender mailSender = mock(JavaMailSender.class);
    EmailService emailService =
        new EmailService(mailSender, true, "no-reply@example.com", "http://localhost:3000/");

    emailService.sendProjectInvitationEmail(invitation());

    org.mockito.ArgumentCaptor<SimpleMailMessage> captor =
        org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(captor.capture());

    SimpleMailMessage message = captor.getValue();
    assertEquals("guest@example.com", message.getTo()[0]);
    assertEquals("no-reply@example.com", message.getFrom());
    assertEquals("Invitation to join Alpha", message.getSubject());
    assertTrue(message.getText().contains("http://localhost:3000/invites/token-123"));
  }

  private ProjectInvitation invitation() {
    Project project = new Project();
    project.setName("Alpha");

    User inviter = new User();
    inviter.setUsername("owner");

    ProjectInvitation invitation = new ProjectInvitation();
    invitation.setProject(project);
    invitation.setInvitedBy(inviter);
    invitation.setInvitedEmail("guest@example.com");
    invitation.setToken("token-123");
    return invitation;
  }
}
