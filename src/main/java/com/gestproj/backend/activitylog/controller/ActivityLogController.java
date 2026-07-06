package com.gestproj.backend.activitylog.controller;

import com.gestproj.backend.activitylog.dto.ActivityLogResponse;
import com.gestproj.backend.activitylog.service.ActivityLogService;
import com.gestproj.backend.common.exception.ResourceNotFoundException;
import com.gestproj.backend.member.service.ProjectMemberService;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.project.repository.ProjectRepository;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/activity-logs")
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final ProjectRepository projectRepository;
    private final ProjectMemberService projectMemberService;
    private final UserService userService;

    public ActivityLogController(
            ActivityLogService activityLogService,
            ProjectRepository projectRepository,
            ProjectMemberService projectMemberService,
            UserService userService
    ) {
        this.activityLogService = activityLogService;
        this.projectRepository = projectRepository;
        this.projectMemberService = projectMemberService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<ActivityLogResponse>> list(@PathVariable Long projectId, Authentication authentication) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        User currentUser = userService.findEntityByEmail(authentication.getName());
        projectMemberService.findProjectMember(project, currentUser);
        return ResponseEntity.ok(activityLogService.listForProject(project));
    }
}
