package com.gestproj.backend.projectinvitation.controller;

import com.gestproj.backend.projectinvitation.dto.ProjectInvitationCreateRequest;
import com.gestproj.backend.projectinvitation.dto.ProjectInvitationResponse;
import com.gestproj.backend.projectinvitation.service.ProjectInvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/invites")
public class ProjectInvitationController {

    private final ProjectInvitationService projectInvitationService;

    public ProjectInvitationController(ProjectInvitationService projectInvitationService) {
        this.projectInvitationService = projectInvitationService;
    }

    @PostMapping
    public ResponseEntity<ProjectInvitationResponse> create(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectInvitationCreateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectInvitationService.create(projectId, request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<ProjectInvitationResponse>> list(@PathVariable Long projectId, Authentication authentication) {
        return ResponseEntity.ok(projectInvitationService.listForProject(projectId, authentication.getName()));
    }
}
