package com.gestproj.backend.member.service;

import com.gestproj.backend.activitylog.service.ActivityLogService;
import com.gestproj.backend.common.enums.NotificationType;
import com.gestproj.backend.common.enums.ProjectMemberRole;
import com.gestproj.backend.common.enums.ProjectMemberStatus;
import com.gestproj.backend.common.exception.ForbiddenException;
import com.gestproj.backend.member.dto.ProjectMemberUpdateRequest;
import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.member.repository.ProjectMemberRepository;
import com.gestproj.backend.notification.service.NotificationService;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private ProjectMemberService projectMemberService;

    @Test
    void addMemberShouldInitializeOwnerPermissions() {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", 7L);
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 11L);

        when(projectMemberRepository.existsByProjectAndUser(project, user)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectMember member = projectMemberService.addMember(project, user, ProjectMemberRole.OWNER);

        assertEquals(ProjectMemberStatus.ACTIVE, member.getStatus());
        assertEquals("Owner", member.getRoleTitle());
        assertEquals("Project owner", member.getRoleDescription());
        assertEquals(true, member.isCanManageMembers());
    }

    @Test
    void updateMemberShouldRejectActorWithoutManagePermission() {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", 7L);

        User actor = new User();
        ReflectionTestUtils.setField(actor, "id", 1L);

        ProjectMember actorMember = new ProjectMember();
        actorMember.setProject(project);
        actorMember.setUser(actor);
        actorMember.setRole(ProjectMemberRole.MEMBER);
        actorMember.setCanManageMembers(false);

        when(projectMemberRepository.findByProjectAndUser(project, actor)).thenReturn(Optional.of(actorMember));

        assertThrows(ForbiddenException.class, () ->
                projectMemberService.updateMember(
                        project,
                        22L,
                        new ProjectMemberUpdateRequest(
                                ProjectMemberRole.MEMBER,
                                ProjectMemberStatus.SUSPENDED,
                                "Tester",
                                "QA role",
                                true,
                                true,
                                false,
                                false,
                                false,
                                false
                        ),
                        actor
                )
        );
    }

    @Test
    void updateMemberShouldPersistAndNotify() {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", 7L);
        project.setName("Alpha");

        User actor = new User();
        ReflectionTestUtils.setField(actor, "id", 1L);

        User target = new User();
        ReflectionTestUtils.setField(target, "id", 2L);
        target.setUsername("member");

        ProjectMember actorMember = new ProjectMember();
        actorMember.setProject(project);
        actorMember.setUser(actor);
        actorMember.setRole(ProjectMemberRole.OWNER);
        actorMember.setCanManageMembers(true);

        ProjectMember targetMember = new ProjectMember();
        targetMember.setProject(project);
        targetMember.setUser(target);
        targetMember.setRole(ProjectMemberRole.MEMBER);
        targetMember.setStatus(ProjectMemberStatus.ACTIVE);
        targetMember.setCanViewProject(true);
        targetMember.setCanCreateTask(true);
        targetMember.setCanEditTask(true);
        targetMember.setCanDeleteTask(false);
        targetMember.setCanInviteMember(false);
        targetMember.setCanManageMembers(false);
        ReflectionTestUtils.setField(targetMember, "id", 22L);

        when(projectMemberRepository.findByProjectAndUser(project, actor)).thenReturn(Optional.of(actorMember));
        when(projectMemberRepository.findById(22L)).thenReturn(Optional.of(targetMember));
        when(projectMemberRepository.save(any(ProjectMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = projectMemberService.updateMember(
                project,
                22L,
                new ProjectMemberUpdateRequest(
                        ProjectMemberRole.MEMBER,
                        ProjectMemberStatus.SUSPENDED,
                        "Tester",
                        "QA role",
                        true,
                        false,
                        false,
                        false,
                        false,
                        false
                ),
                actor
        );

        assertEquals(ProjectMemberStatus.SUSPENDED, response.status());
        assertEquals("Tester", response.roleTitle());
        verify(activityLogService).log(eq(project), eq(actor), eq("Updated member member"));
        verify(notificationService).notify(
                eq(target),
                eq(NotificationType.MEMBER_STATUS_CHANGED),
                eq("Project member updated"),
                eq("Your role or permissions were updated in project Alpha"),
                eq(project),
                any(),
                eq(targetMember)
        );
    }
}
