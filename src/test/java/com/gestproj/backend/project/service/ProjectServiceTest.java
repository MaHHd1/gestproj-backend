package com.gestproj.backend.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.gestproj.backend.activitylog.service.ActivityLogService;
import com.gestproj.backend.common.enums.ProjectMemberRole;
import com.gestproj.backend.common.exception.ForbiddenException;
import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.member.service.ProjectMemberService;
import com.gestproj.backend.project.dto.ProjectCreateRequest;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.project.repository.ProjectRepository;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock private ProjectRepository projectRepository;

  @Mock private ProjectMemberService projectMemberService;

  @Mock private ActivityLogService activityLogService;

  @Mock private UserService userService;

  @InjectMocks private ProjectService projectService;

  @Test
  void createShouldSaveProjectAndAddOwnerMembership() {
    User owner = new User();
    owner.setEmail("owner@example.com");
    owner.setUsername("owner");
    ReflectionTestUtils.setField(owner, "id", 1L);

    when(userService.findEntityByEmail("owner@example.com")).thenReturn(owner);
    when(projectRepository.save(any(Project.class)))
        .thenAnswer(
            invocation -> {
              Project project = invocation.getArgument(0);
              ReflectionTestUtils.setField(project, "id", 10L);
              return project;
            });

    var response =
        projectService.create(
            new ProjectCreateRequest("Team Board", "Main project"), "owner@example.com");

    assertEquals(10L, response.id());
    assertEquals(1L, response.ownerId());
    assertEquals("owner", response.ownerUsername());
    verify(projectMemberService)
        .addMember(any(Project.class), eq(owner), eq(ProjectMemberRole.OWNER));
    verify(activityLogService).log(any(Project.class), eq(owner), eq("Created project"));
  }

  @Test
  void updateShouldRejectNonOwner() {
    User owner = new User();
    ReflectionTestUtils.setField(owner, "id", 1L);
    User currentUser = new User();
    ReflectionTestUtils.setField(currentUser, "id", 2L);

    Project project = new Project();
    ReflectionTestUtils.setField(project, "id", 10L);
    project.setOwner(owner);

    when(userService.findEntityByEmail("user@example.com")).thenReturn(currentUser);
    when(projectRepository.findById(10L)).thenReturn(java.util.Optional.of(project));

    assertThrows(
        ForbiddenException.class,
        () ->
            projectService.update(
                10L,
                new com.gestproj.backend.project.dto.ProjectUpdateRequest("Updated", "Desc"),
                "user@example.com"));
  }

  @Test
  void getProjectsForCurrentUserShouldMapMembershipProjects() {
    User user = new User();
    ReflectionTestUtils.setField(user, "id", 1L);
    user.setEmail("member@example.com");

    Project project = new Project();
    ReflectionTestUtils.setField(project, "id", 5L);
    project.setName("Alpha");
    project.setOwner(user);

    ProjectMember member = new ProjectMember();
    member.setProject(project);

    when(userService.findEntityByEmail("member@example.com")).thenReturn(user);
    when(projectMemberService.getMembersForUser(user)).thenReturn(List.of(member));

    var projects = projectService.getProjectsForCurrentUser("member@example.com");

    assertEquals(1, projects.size());
    assertEquals(5L, projects.get(0).id());
    assertEquals("Alpha", projects.get(0).name());
  }
}
