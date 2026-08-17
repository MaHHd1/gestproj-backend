package com.gestproj.backend.workflow.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestproj.backend.common.exception.ForbiddenException;
import com.gestproj.backend.common.exception.ResourceNotFoundException;
import com.gestproj.backend.gitea.GiteaClient;
import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.member.service.ProjectMemberService;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.project.repository.ProjectRepository;
import com.gestproj.backend.user.service.UserService;
import com.gestproj.backend.workflow.dto.WorkflowJobResponse;
import com.gestproj.backend.workflow.dto.WorkflowRunResponse;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Service
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class WorkflowService {
  private final ProjectRepository projectRepository;
  private final ProjectMemberService memberService;
  private final UserService userService;
  private final GiteaClient giteaClient;

  public WorkflowService(
      ProjectRepository projectRepository,
      ProjectMemberService memberService,
      UserService userService,
      GiteaClient giteaClient) {
    this.projectRepository = projectRepository;
    this.memberService = memberService;
    this.userService = userService;
    this.giteaClient = giteaClient;
  }

  @Transactional(readOnly = true)
  public List<WorkflowRunResponse> getRuns(Long projectId, String email) {
    Project project = accessibleProject(projectId, email);
    return giteaClient.getWorkflowRuns(project.getRepoOwner(), project.getRepoName());
  }

  @Transactional(readOnly = true)
  public List<WorkflowJobResponse> getJobs(Long projectId, Long runId, String email) {
    Project project = accessibleProject(projectId, email);
    return giteaClient.getWorkflowJobs(project.getRepoOwner(), project.getRepoName(), runId);
  }

  @Transactional(readOnly = true)
  public String getJobLogs(Long projectId, Long runId, Long jobId, String email) {
    Project project = accessibleProject(projectId, email);
    return giteaClient.getWorkflowJobLogs(
        project.getRepoOwner(), project.getRepoName(), runId, jobId);
  }

  private Project accessibleProject(Long projectId, String email) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    ProjectMember membership =
        memberService.findProjectMember(project, userService.findEntityByEmail(email));
    if (!membership.isCanViewProject()) {
      throw new ForbiddenException("You are not allowed to access this project");
    }
    if (project.getRepoOwner() == null
        || project.getRepoOwner().isBlank()
        || project.getRepoName() == null
        || project.getRepoName().isBlank()) {
      return project;
    }
    return project;
  }
}
