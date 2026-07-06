package com.gestproj.backend.projectinvitation.controller;

import com.gestproj.backend.projectinvitation.dto.ProjectInvitationResponse;
import com.gestproj.backend.projectinvitation.service.ProjectInvitationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invites")
public class ProjectInvitationActionController {

    private final ProjectInvitationService projectInvitationService;

    public ProjectInvitationActionController(ProjectInvitationService projectInvitationService) {
        this.projectInvitationService = projectInvitationService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<ProjectInvitationResponse> getByToken(@PathVariable String token, Authentication authentication) {
        return ResponseEntity.ok(projectInvitationService.getByToken(token, authentication.getName()));
    }

    @PostMapping("/{token}/accept")
    public ResponseEntity<ProjectInvitationResponse> accept(@PathVariable String token, Authentication authentication) {
        return ResponseEntity.ok(projectInvitationService.accept(token, authentication.getName()));
    }

    @PostMapping("/{token}/reject")
    public ResponseEntity<ProjectInvitationResponse> reject(@PathVariable String token, Authentication authentication) {
        return ResponseEntity.ok(projectInvitationService.reject(token, authentication.getName()));
    }
}
