package com.gestproj.backend.common.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.projectinvitation.entity.ProjectInvitation;
import com.gestproj.backend.user.entity.User;

@Service
public class EmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

  private final JavaMailSender mailSender;
  private final boolean mailEnabled;
  private final String fromAddress;
  private final String frontendUrl;

  public EmailService(
      JavaMailSender mailSender,
      @Value("${app.mail.enabled:false}") boolean mailEnabled,
      @Value("${app.mail.from:no-reply@gestproj.local}") String fromAddress,
      @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
    this.mailSender = mailSender;
    this.mailEnabled = mailEnabled;
    this.fromAddress = fromAddress;
    this.frontendUrl = trimTrailingSlash(frontendUrl);
  }

  public void sendProjectInvitationEmail(ProjectInvitation invitation) {
    String recipient = invitation.getInvitedEmail();
    if (!mailEnabled || !StringUtils.hasText(recipient)) {
      return;
    }

    Project project = invitation.getProject();
    User inviter = invitation.getInvitedBy();
    String invitationUrl = frontendUrl + "/invites/" + invitation.getToken();

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(recipient);
    message.setSubject("Invitation to join " + project.getName());
    message.setText(buildInvitationBody(inviter, project, invitationUrl));

    try {
      mailSender.send(message);
    } catch (MailException ex) {
      LOGGER.warn("Failed to send project invitation email to {}", recipient, ex);
    }
  }

  private String buildInvitationBody(User inviter, Project project, String invitationUrl) {
    return """
                Hello,

                %s invited you to join the project "%s" on GestProj.

                Open this link to view and respond to the invitation:
                %s

                This invitation expires in 7 days.
                """
        .formatted(inviter.getUsername(), project.getName(), invitationUrl);
  }

  private String trimTrailingSlash(String value) {
    if (!StringUtils.hasText(value)) {
      return "http://localhost:3000";
    }
    return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
  }
}
