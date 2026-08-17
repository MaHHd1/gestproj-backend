package com.gestproj.backend.deployment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestproj.backend.activitylog.service.ActivityLogService;
import com.gestproj.backend.common.enums.NotificationType;
import com.gestproj.backend.common.exception.ForbiddenException;
import com.gestproj.backend.common.exception.ResourceNotFoundException;
import com.gestproj.backend.deployment.dto.DeploymentCreateRequest;
import com.gestproj.backend.deployment.dto.DeploymentResponse;
import com.gestproj.backend.deployment.entity.Deployment;
import com.gestproj.backend.deployment.repository.DeploymentRepository;
import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.member.service.ProjectMemberService;
import com.gestproj.backend.notification.service.NotificationService;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.project.repository.ProjectRepository;
import com.gestproj.backend.user.service.UserService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Service
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class DeploymentService {

  private final DeploymentRepository deploymentRepository;
  private final ProjectRepository projectRepository;
  private final ProjectMemberService projectMemberService;
  private final UserService userService;
  private final ActivityLogService activityLogService;
  private final NotificationService notificationService;

  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public DeploymentService(
      DeploymentRepository deploymentRepository,
      ProjectRepository projectRepository,
      ProjectMemberService projectMemberService,
      UserService userService,
      ActivityLogService activityLogService,
      NotificationService notificationService) {
    this.deploymentRepository = deploymentRepository;
    this.projectRepository = projectRepository;
    this.projectMemberService = projectMemberService;
    this.userService = userService;
    this.activityLogService = activityLogService;
    this.notificationService = notificationService;
  }

  @Transactional
  public DeploymentResponse recordDeployment(
      Long projectId, DeploymentCreateRequest request, String actorEmail) {
    Project project = getAccessibleProject(projectId, actorEmail, true);

    Deployment d = new Deployment();
    d.setProject(project);
    d.setStatus(request.status());
    d.setCommitHash(request.commitHash());
    d.setCommitMessage(request.commitMessage());
    d.setTriggeredBy(request.triggeredBy());
    LocalDateTime now = LocalDateTime.now();
    d.setStartedAt(now);
    d.setFinishedAt(isTerminalStatus(request.status()) ? now : null);
    d.setWorkflowName(request.workflowName());
    d.setWorkflowRunId(request.workflowRunId());
    d.setWorkflowUrl(request.workflowUrl());
    d.setDeploymentTarget(request.deploymentTarget());
    d.setDockerStatus(request.dockerStatus());
    d.setDockerDetails(request.dockerDetails());

    Deployment saved = deploymentRepository.save(d);

    // Log activity
    var actor = userService.findEntityByEmail(actorEmail);
    if ("SUCCESS".equalsIgnoreCase(saved.getStatus())) {
      activityLogService.log(project, actor, "Deployment succeeded: " + saved.getCommitMessage());
      notificationService.notify(
          project.getOwner(),
          NotificationType.DEPLOYMENT_SUCCEEDED,
          "Deployment succeeded",
          "Deployment succeeded: " + safeCommitMessage(saved),
          project,
          null,
          null);
    } else if ("FAILURE".equalsIgnoreCase(saved.getStatus())) {
      activityLogService.log(project, actor, "Deployment failed: " + saved.getCommitMessage());
      // Notify project owner about failure
      notificationService.notify(
          project.getOwner(),
          NotificationType.DEPLOYMENT_FAILED,
          "Deployment failed",
          "Deployment failed: " + safeCommitMessage(saved),
          project,
          null,
          null);
    }

    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<DeploymentResponse> getDeployments(Long projectId, String actorEmail) {
    Project project = getAccessibleProject(projectId, actorEmail, false);
    return deploymentRepository.findAllByProjectIdOrderByFinishedAtDesc(project.getId()).stream()
        .map(this::toResponse)
        .toList();
  }

  private DeploymentResponse toResponse(Deployment d) {
    return new DeploymentResponse(
        d.getId(),
        d.getProject().getId(),
        d.getStatus(),
        d.getCommitHash(),
        d.getCommitMessage(),
        d.getTriggeredBy(),
        d.getStartedAt(),
        d.getFinishedAt(),
        d.getWorkflowName(),
        d.getWorkflowRunId(),
        d.getWorkflowUrl(),
        d.getDeploymentTarget(),
        d.getDockerStatus(),
        d.getDockerDetails());
  }

  private boolean isTerminalStatus(String status) {
    return "SUCCESS".equalsIgnoreCase(status) || "FAILURE".equalsIgnoreCase(status);
  }

  private String safeCommitMessage(Deployment deployment) {
    return deployment.getCommitMessage() == null || deployment.getCommitMessage().isBlank()
        ? "No commit message"
        : deployment.getCommitMessage();
  }

  private Project getAccessibleProject(Long projectId, String actorEmail, boolean needWriteAccess) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    ProjectMember actorMember = getMember(project, actorEmail);

    if (!actorMember.isCanViewProject()
        && actorMember.getRole() != com.gestproj.backend.common.enums.ProjectMemberRole.OWNER) {
      throw new ForbiddenException("You are not allowed to access this project");
    }

    if (needWriteAccess
        && !actorMember.isCanCreateTask()
        && actorMember.getRole() != com.gestproj.backend.common.enums.ProjectMemberRole.OWNER) {
      throw new ForbiddenException("You are not allowed to create deployments in this project");
    }

    return project;
  }

  private ProjectMember getMember(Project project, String actorEmail) {
    return projectMemberService.findProjectMember(
        project, userService.findEntityByEmail(actorEmail));
  }
}
