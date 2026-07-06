package com.gestproj.backend.project.service;

import com.gestproj.backend.common.enums.ProjectMemberRole;
import com.gestproj.backend.common.exception.ForbiddenException;
import com.gestproj.backend.common.exception.ResourceNotFoundException;
import com.gestproj.backend.member.service.ProjectMemberService;
import com.gestproj.backend.project.dto.ProjectCreateRequest;
import com.gestproj.backend.project.dto.ProjectResponse;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.project.repository.ProjectRepository;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberService projectMemberService;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, ProjectMemberService projectMemberService, UserService userService) {
        this.projectRepository = projectRepository;
        this.projectMemberService = projectMemberService;
        this.userService = userService;
    }

    public ProjectResponse create(ProjectCreateRequest request, String currentUserEmail) {
        User currentUser = userService.findEntityByEmail(currentUserEmail);

        Project project = new Project();
        project.setName(request.name().trim());
        project.setDescription(request.description() == null ? null : request.description().trim());
        project.setOwner(currentUser);

        Project savedProject = projectRepository.save(project);
        projectMemberService.addMember(savedProject, currentUser, ProjectMemberRole.OWNER);

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

    public ProjectResponse update(Long id, ProjectCreateRequest request, String currentUserEmail) {
        User currentUser = userService.findEntityByEmail(currentUserEmail);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the project owner can update this project");
        }

        project.setName(request.name().trim());
        project.setDescription(request.description() == null ? null : request.description().trim());
        return toResponse(projectRepository.save(project));
    }

    public void delete(Long id, String currentUserEmail) {
        User currentUser = userService.findEntityByEmail(currentUserEmail);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the project owner can delete this project");
        }

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
