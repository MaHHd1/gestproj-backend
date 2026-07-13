package com.gestproj.backend.member.controller;

import com.gestproj.backend.member.dto.ProjectMemberResponse;
import com.gestproj.backend.member.dto.ProjectMemberUpdateRequest;
import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.member.service.ProjectMemberService;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.project.repository.ProjectRepository;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectRepository projectRepository;
    private final ProjectMemberService projectMemberService;
    private final UserService userService;

    public ProjectMemberController(ProjectRepository projectRepository, ProjectMemberService projectMemberService, UserService userService) {
        this.projectRepository = projectRepository;
        this.projectMemberService = projectMemberService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectMemberResponse>> list(@PathVariable Long projectId, Authentication authentication) {
        Project project = getProject(projectId);
        User currentUser = userService.findEntityByEmail(authentication.getName());
        ensureMember(project, currentUser);
        return ResponseEntity.ok(projectMemberService.getProjectMembers(project));
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<ProjectMemberResponse> update(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @Valid @RequestBody ProjectMemberUpdateRequest request,
            Authentication authentication
    ) {
        Project project = getProject(projectId);
        User currentUser = userService.findEntityByEmail(authentication.getName());
        return ResponseEntity.ok(projectMemberService.updateMember(project, memberId, request, currentUser));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> remove(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            Authentication authentication
    ) {
        Project project = getProject(projectId);
        User currentUser = userService.findEntityByEmail(authentication.getName());
        projectMemberService.removeMember(project, memberId, currentUser);
        return ResponseEntity.noContent().build();
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new com.gestproj.backend.common.exception.ResourceNotFoundException("Project not found"));
    }

    private void ensureMember(Project project, User user) {
        ProjectMember member = projectMemberService.findProjectMember(project, user);
        if (member == null) {
            throw new com.gestproj.backend.common.exception.ForbiddenException("You are not allowed to access this project");
        }
    }
}
