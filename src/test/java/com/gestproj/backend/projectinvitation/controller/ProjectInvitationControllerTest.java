package com.gestproj.backend.projectinvitation.controller;

import com.gestproj.backend.common.enums.ProjectInvitationStatus;
import com.gestproj.backend.common.enums.ProjectMemberRole;
import com.gestproj.backend.projectinvitation.dto.ProjectInvitationCreateRequest;
import com.gestproj.backend.projectinvitation.dto.ProjectInvitationResponse;
import com.gestproj.backend.projectinvitation.service.ProjectInvitationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectInvitationControllerTest {

    @Mock
    private ProjectInvitationService projectInvitationService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectInvitationController controller;

    @InjectMocks
    private ProjectInvitationActionController actionController;

    @Test
    void createShouldReturnInvitation() {
        when(authentication.getName()).thenReturn("owner@example.com");
        when(projectInvitationService.create(eq(1L), any(ProjectInvitationCreateRequest.class), eq("owner@example.com"))).thenReturn(
                new ProjectInvitationResponse(1L, 1L, 2L, "guest@example.com", "token-1", ProjectInvitationStatus.PENDING, LocalDateTime.now(), ProjectMemberRole.MEMBER, "Tester", "QA", true, true, false, false, false, false)
        );

        var response = controller.create(
                1L,
                new ProjectInvitationCreateRequest("guest@example.com", ProjectMemberRole.MEMBER, "Tester", "QA", true, true, false, false, false, false),
                authentication
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("token-1", response.getBody().token());
    }

    @Test
    void listShouldReturnInvitations() {
        when(authentication.getName()).thenReturn("owner@example.com");
        when(projectInvitationService.listForProject(1L, "owner@example.com")).thenReturn(
                List.of(new ProjectInvitationResponse(1L, 1L, 2L, "guest@example.com", "token-1", ProjectInvitationStatus.PENDING, LocalDateTime.now(), ProjectMemberRole.MEMBER, "Tester", "QA", true, true, false, false, false, false))
        );

        var response = controller.list(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void acceptShouldReturnAcceptedInvite() {
        when(authentication.getName()).thenReturn("guest@example.com");
        when(projectInvitationService.accept("token-1", "guest@example.com")).thenReturn(
                new ProjectInvitationResponse(1L, 1L, 2L, "guest@example.com", "token-1", ProjectInvitationStatus.ACCEPTED, LocalDateTime.now(), ProjectMemberRole.MEMBER, "Tester", "QA", true, true, false, false, false, false)
        );

        var response = actionController.accept("token-1", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ProjectInvitationStatus.ACCEPTED, response.getBody().status());
    }
}
