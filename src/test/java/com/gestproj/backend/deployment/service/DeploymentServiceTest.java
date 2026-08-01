package com.gestproj.backend.deployment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.gestproj.backend.deployment.dto.DeploymentCreateRequest;
import com.gestproj.backend.deployment.dto.DeploymentResponse;
import com.gestproj.backend.deployment.entity.Deployment;
import com.gestproj.backend.deployment.repository.DeploymentRepository;
import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.member.service.ProjectMemberService;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.project.repository.ProjectRepository;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class DeploymentServiceTest {

  @Mock private DeploymentRepository deploymentRepository;

  @Mock private ProjectRepository projectRepository;

  @Mock private ProjectMemberService projectMemberService;

  @Mock private UserService userService;

  @InjectMocks private DeploymentService deploymentService;

  @Test
  void recordDeploymentShouldSaveAndReturnResponse() {
    Project project = new Project();
    ReflectionTestUtils.setField(project, "id", 5L);

    User user = new User();
    user.setEmail("actor@example.com");
    ReflectionTestUtils.setField(user, "id", 2L);

    ProjectMember member = new ProjectMember();
    member.setProject(project);
    member.setUser(user);
    member.setCanViewProject(true);
    ReflectionTestUtils.setField(member, "id", 7L);

    when(projectRepository.findById(5L)).thenReturn(java.util.Optional.of(project));
    when(userService.findEntityByEmail("actor@example.com")).thenReturn(user);
    when(projectMemberService.findProjectMember(project, user)).thenReturn(member);
    when(deploymentRepository.save(any(Deployment.class)))
        .thenAnswer(invocation -> {
          Deployment d = invocation.getArgument(0);
          ReflectionTestUtils.setField(d, "id", 11L);
          return d;
        });

    var response = deploymentService.recordDeployment(5L, new DeploymentCreateRequest("SUCCESS", "abc123", "Deployed", "actor"), "actor@example.com");

    assertEquals(11L, response.id());
    assertEquals(5L, response.projectId());
    assertEquals("SUCCESS", response.status());
  }

  @Test
  void getDeploymentsShouldReturnList() {
    Project project = new Project();
    ReflectionTestUtils.setField(project, "id", 5L);

    User user = new User();
    user.setEmail("actor@example.com");
    ReflectionTestUtils.setField(user, "id", 2L);

    ProjectMember member = new ProjectMember();
    member.setProject(project);
    member.setUser(user);
    member.setCanViewProject(true);

    Deployment d = new Deployment();
    ReflectionTestUtils.setField(d, "id", 11L);
    d.setProject(project);
    d.setStatus("SUCCESS");
    d.setCommitHash("abc123");
    d.setFinishedAt(LocalDateTime.now());

    when(projectRepository.findById(5L)).thenReturn(java.util.Optional.of(project));
    when(userService.findEntityByEmail("actor@example.com")).thenReturn(user);
    when(projectMemberService.findProjectMember(project, user)).thenReturn(member);
    when(deploymentRepository.findAllByProjectIdOrderByFinishedAtDesc(5L)).thenReturn(List.of(d));

    List<DeploymentResponse> result = deploymentService.getDeployments(5L, "actor@example.com");

    assertEquals(1, result.size());
    assertEquals(11L, result.get(0).id());
  }
}
