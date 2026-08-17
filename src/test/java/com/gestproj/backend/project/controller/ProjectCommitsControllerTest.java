package com.gestproj.backend.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import com.gestproj.backend.gitea.GiteaClient;
import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.member.service.ProjectMemberService;
import com.gestproj.backend.project.dto.CommitResponse;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.project.repository.ProjectRepository;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class ProjectCommitsControllerTest {

  @Mock private ProjectRepository projectRepository;

  @Mock private ProjectMemberService projectMemberService;

  @Mock private UserService userService;

  @Mock private GiteaClient giteaClient;

  @Mock private Authentication authentication;

  @InjectMocks private ProjectCommitsController controller;

  @Test
  void listShouldReturnEmptyWhenNoRepo() {
    when(authentication.getName()).thenReturn("user@example.com");
    Project project = new Project();
    project.setRepoOwner(null);
    project.setRepoName(null);
    when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
    when(userService.findEntityByEmail("user@example.com")).thenReturn(new User());
    // projectMemberService.findProjectMember should not throw

    var response = controller.list(1L, authentication);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(0, response.getBody().size());
  }

  @Test
  void listShouldReturnCommitsWhenRepoSet() {
    when(authentication.getName()).thenReturn("user@example.com");
    Project project = new Project();
    project.setRepoOwner("org");
    project.setRepoName("repo");
    when(projectRepository.findById(2L)).thenReturn(java.util.Optional.of(project));
    User user = new User();
    when(userService.findEntityByEmail("user@example.com")).thenReturn(user);
    when(projectMemberService.findProjectMember(project, user)).thenReturn(new ProjectMember());
    when(giteaClient.getCommits("org", "repo"))
        .thenReturn(List.of(new CommitResponse("sha1", "msg", "author", OffsetDateTime.now())));

    var response = controller.list(2L, authentication);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }
}
