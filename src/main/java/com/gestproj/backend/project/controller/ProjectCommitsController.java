package com.gestproj.backend.project.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestproj.backend.gitea.GiteaClient;
import com.gestproj.backend.project.dto.CommitResponse;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.project.repository.ProjectRepository;
import com.gestproj.backend.user.service.UserService;
import com.gestproj.backend.member.service.ProjectMemberService;

@RestController
@RequestMapping("/projects/{projectId}/commits")
public class ProjectCommitsController {

  private static final Logger log = LoggerFactory.getLogger(ProjectCommitsController.class);

  private final ProjectRepository projectRepository;
  private final ProjectMemberService projectMemberService;
  private final UserService userService;
  private final GiteaClient giteaClient;

  public ProjectCommitsController(
      ProjectRepository projectRepository,
      ProjectMemberService projectMemberService,
      UserService userService,
      GiteaClient giteaClient) {
    this.projectRepository = projectRepository;
    this.projectMemberService = projectMemberService;
    this.userService = userService;
    this.giteaClient = giteaClient;
  }

  @GetMapping
  public ResponseEntity<List<CommitResponse>> list(@PathVariable Long projectId, Authentication authentication) {
    Project project = projectRepository.findById(projectId).orElse(null);
    if (project == null) {
      return ResponseEntity.notFound().build();
    }

    // enforce active membership
    projectMemberService.findProjectMember(project, userService.findEntityByEmail(authentication.getName()));

    if (project.getRepoOwner() == null || project.getRepoName() == null) {
      return ResponseEntity.ok(List.of());
    }

    List<CommitResponse> commits = giteaClient.getCommits(project.getRepoOwner(), project.getRepoName());
    return ResponseEntity.ok(commits);
  }
}
