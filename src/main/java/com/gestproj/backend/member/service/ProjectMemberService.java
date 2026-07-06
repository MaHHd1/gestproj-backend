package com.gestproj.backend.member.service;

import com.gestproj.backend.common.enums.ProjectMemberRole;
import com.gestproj.backend.common.enums.ProjectMemberStatus;
import com.gestproj.backend.common.enums.NotificationType;
import com.gestproj.backend.common.exception.ConflictException;
import com.gestproj.backend.common.exception.ForbiddenException;
import com.gestproj.backend.common.exception.ResourceNotFoundException;
import com.gestproj.backend.activitylog.service.ActivityLogService;
import com.gestproj.backend.member.dto.ProjectMemberResponse;
import com.gestproj.backend.member.dto.ProjectMemberUpdateRequest;
import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.member.repository.ProjectMemberRepository;
import com.gestproj.backend.notification.service.NotificationService;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository, NotificationService notificationService, ActivityLogService activityLogService) {
        this.projectMemberRepository = projectMemberRepository;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    public ProjectMember addMember(Project project, User user, ProjectMemberRole role) {
        if (projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new ConflictException("User is already a member of this project");
        }

        ProjectMember projectMember = new ProjectMember();
        projectMember.setProject(project);
        projectMember.setUser(user);
        projectMember.setRole(role);
        projectMember.setStatus(role == ProjectMemberRole.OWNER ? ProjectMemberStatus.ACTIVE : ProjectMemberStatus.INVITED);
        projectMember.setRoleTitle(role == ProjectMemberRole.OWNER ? "Owner" : "Member");
        projectMember.setRoleDescription(role == ProjectMemberRole.OWNER ? "Project owner" : "Project member");
        projectMember.setCanViewProject(true);
        projectMember.setCanCreateTask(true);
        projectMember.setCanEditTask(true);
        projectMember.setCanDeleteTask(role == ProjectMemberRole.OWNER);
        projectMember.setCanInviteMember(role == ProjectMemberRole.OWNER);
        projectMember.setCanManageMembers(role == ProjectMemberRole.OWNER);
        return projectMemberRepository.save(projectMember);
    }

    public boolean isMember(Project project, User user) {
        return projectMemberRepository.existsByProjectAndUser(project, user);
    }

    public List<ProjectMember> getMembersForUser(User user) {
        return projectMemberRepository.findAllByUser(user);
    }

    public List<ProjectMemberResponse> getProjectMembers(Project project) {
        return projectMemberRepository.findAllByProject(project).stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectMemberResponse updateMember(Project project, Long memberId, ProjectMemberUpdateRequest request, User actor) {
        ProjectMember actorMember = projectMemberRepository.findByProjectAndUser(project, actor)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this project"));

        if (actorMember.getRole() != ProjectMemberRole.OWNER && !actorMember.isCanManageMembers()) {
            throw new ForbiddenException("You are not allowed to manage project members");
        }

        ProjectMember member = projectMemberRepository.findById(memberId)
                .filter(existing -> existing.getProject().getId().equals(project.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Project member not found"));

        if (member.getRole() == ProjectMemberRole.OWNER && request.role() != ProjectMemberRole.OWNER) {
            throw new ForbiddenException("Project owner role cannot be downgraded here");
        }

        ProjectMemberStatus originalStatus = member.getStatus();
        if (request.role() != null) {
            member.setRole(request.role());
        }
        if (request.status() != null) {
            member.setStatus(request.status());
        }
        if (request.roleTitle() != null) {
            member.setRoleTitle(request.roleTitle().trim());
        }
        if (request.roleDescription() != null) {
            member.setRoleDescription(request.roleDescription().trim());
        }
        member.setCanViewProject(request.canViewProject());
        member.setCanCreateTask(request.canCreateTask());
        member.setCanEditTask(request.canEditTask());
        member.setCanDeleteTask(request.canDeleteTask());
        member.setCanInviteMember(request.canInviteMember());
        member.setCanManageMembers(request.canManageMembers());

        ProjectMember savedMember = projectMemberRepository.save(member);
        activityLogService.log(project, actor, "Updated member " + savedMember.getUser().getUsername());
        notificationService.notify(
                savedMember.getUser(),
                request.status() != null && request.status() != originalStatus
                        ? NotificationType.MEMBER_STATUS_CHANGED
                        : NotificationType.MEMBER_UPDATED,
                "Project member updated",
                "Your role or permissions were updated in project " + project.getName(),
                project,
                null,
                savedMember
        );
        return toResponse(savedMember);
    }

    public ProjectMember findProjectMember(Project project, User user) {
        return projectMemberRepository.findByProjectAndUser(project, user)
                .orElseThrow(() -> new ResourceNotFoundException("Project member not found"));
    }

    private ProjectMemberResponse toResponse(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getProject().getId(),
                member.getUser().getId(),
                member.getUser().getUsername(),
                member.getRole(),
                member.getStatus(),
                member.getRoleTitle(),
                member.getRoleDescription(),
                member.isCanViewProject(),
                member.isCanCreateTask(),
                member.isCanEditTask(),
                member.isCanDeleteTask(),
                member.isCanInviteMember(),
                member.isCanManageMembers()
        );
    }
}
