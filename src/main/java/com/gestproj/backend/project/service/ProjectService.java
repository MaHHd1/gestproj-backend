package com.gestproj.backend.project.service;

import com.gestproj.backend.common.enums.ProjectMemberRole;
import com.gestproj.backend.common.enums.TaskStatus;
import com.gestproj.backend.common.exception.ForbiddenException;
import com.gestproj.backend.common.exception.ResourceNotFoundException;
import com.gestproj.backend.activitylog.service.ActivityLogService;
import com.gestproj.backend.project.dto.ProjectUpdateRequest;
import com.gestproj.backend.member.service.ProjectMemberService;
import com.gestproj.backend.project.dto.ProjectCreateRequest;
import com.gestproj.backend.project.dto.ProjectResponse;
import com.gestproj.backend.project.dto.ProjectStatisticsResponse;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.project.repository.ProjectRepository;
import com.gestproj.backend.task.repository.TaskRepository;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberService projectMemberService;
    private final ActivityLogService activityLogService;
    private final UserService userService;
    private final TaskRepository taskRepository;

    public ProjectService(ProjectRepository projectRepository, ProjectMemberService projectMemberService, ActivityLogService activityLogService, UserService userService, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberService = projectMemberService;
        this.activityLogService = activityLogService;
        this.userService = userService;
        this.taskRepository = taskRepository;
    }

    public ProjectResponse create(ProjectCreateRequest request, String currentUserEmail) {
        User currentUser = userService.findEntityByEmail(currentUserEmail);

        Project project = new Project();
        project.setName(request.name().trim());
        project.setDescription(request.description() == null ? null : request.description().trim());
        project.setOwner(currentUser);

        Project savedProject = projectRepository.save(project);
        projectMemberService.addMember(savedProject, currentUser, ProjectMemberRole.OWNER);
        activityLogService.log(savedProject, currentUser, "Created project");

        return toResponse(savedProject);
    }

    public List<ProjectResponse> getProjectsForCurrentUser(String currentUserEmail) {
        User currentUser = userService.findEntityByEmail(currentUserEmail);
        return projectMemberService.getMembersForUser(currentUser).stream()
                .map(member -> toResponse(member.getProject()))
                .toList();
    }

    public ProjectResponse getById(Long id, String currentUserEmail) {
        User currentUser = userService.findEntityByEmail(currentUserEmail);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!projectMemberService.isMember(project, currentUser)) {
            throw new ResourceNotFoundException("Project not found");
        }

        return toResponse(project);
    }

    public ProjectStatisticsResponse getStatistics(Long projectId, String currentUserEmail) {
        User currentUser = userService.findEntityByEmail(currentUserEmail);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!projectMemberService.isMember(project, currentUser)) {
            throw new ResourceNotFoundException("Project not found");
        }

        List<com.gestproj.backend.task.entity.Task> allTasks = project.getTasks();

        long totalTasks = allTasks.size();
        long completedTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.TERMINE).count();
        long inProgressTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.EN_COURS).count();
        long notStartedTasks = allTasks.stream().filter(t -> t.getStatus() == TaskStatus.A_FAIRE).count();
        long lateTasks = allTasks.stream().filter(com.gestproj.backend.task.entity.Task::isLate).count();

        double completionPercentage = totalTasks == 0 ? 0.0 : (double) completedTasks / totalTasks * 100;

        return new ProjectStatisticsResponse(
                totalTasks,
                completedTasks,
                inProgressTasks,
                notStartedTasks,
                lateTasks,
                Math.round(completionPercentage * 100.0) / 100.0
        );
    }

    public ProjectResponse update(Long id, ProjectUpdateRequest request, String currentUserEmail) {
        User currentUser = userService.findEntityByEmail(currentUserEmail);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the project owner can update this project");
        }

        project.setName(request.name().trim());
        project.setDescription(request.description() == null ? null : request.description().trim());
        Project savedProject = projectRepository.save(project);
        activityLogService.log(savedProject, currentUser, "Updated project");
        return toResponse(savedProject);
    }

    public void delete(Long id, String currentUserEmail) {
        User currentUser = userService.findEntityByEmail(currentUserEmail);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the project owner can delete this project");
        }

        activityLogService.log(project, currentUser, "Deleted project");
        projectRepository.delete(project);
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwner().getId(),
                project.getOwner().getUsername()
        );
    }
}
