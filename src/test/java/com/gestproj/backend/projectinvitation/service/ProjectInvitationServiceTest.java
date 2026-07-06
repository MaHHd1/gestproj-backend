package com.gestproj.backend.projectinvitation.service;

import com.gestproj.backend.activitylog.service.ActivityLogService;
import com.gestproj.backend.common.enums.ProjectInvitationStatus;
import com.gestproj.backend.common.enums.ProjectMemberRole;
import com.gestproj.backend.common.enums.ProjectMemberStatus;
import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.member.repository.ProjectMemberRepository;
import com.gestproj.backend.member.service.ProjectMemberService;
import com.gestproj.backend.notification.service.NotificationService;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.project.repository.ProjectRepository;
import com.gestproj.backend.projectinvitation.dto.ProjectInvitationCreateRequest;
import com.gestproj.backend.projectinvitation.entity.ProjectInvitation;
import com.gestproj.backend.projectinvitation.repository.ProjectInvitationRepository;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.repository.UserRepository;
import com.gestproj.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class ProjectInvitationServiceTest {

    @Mock
    private ProjectInvitationRepository projectInvitationRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectMemberService projectMemberService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectInvitationService projectInvitationService;

    @Test
    void createShouldSaveInvitationAndNotifyKnownUser() {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", 4L);
        project.setName("Alpha");

        User actor = new User();
        ReflectionTestUtils.setField(actor, "id", 1L);
        actor.setUsername("owner");

        User invited = new User();
        ReflectionTestUtils.setField(invited, "id", 2L);
        invited.setEmail("guest@example.com");

        ProjectMember actorMember = new ProjectMember();
        actorMember.setProject(project);
        actorMember.setUser(actor);
        actorMember.setRole(ProjectMemberRole.OWNER);
        actorMember.setStatus(ProjectMemberStatus.ACTIVE);
        actorMember.setCanInviteMember(true);

        when(projectRepository.findById(4L)).thenReturn(Optional.of(project));
        when(userService.findEntityByEmail("owner@example.com")).thenReturn(actor);
        when(projectMemberService.findProjectMember(project, actor)).thenReturn(actorMember);
        when(userRepository.findByEmail("guest@example.com")).thenReturn(Optional.of(invited));
        when(projectMemberRepository.existsByProjectAndUser(project, invited)).thenReturn(false);
        when(projectInvitationRepository.save(any(ProjectInvitation.class))).thenAnswer(invocation -> {
            ProjectInvitation invitation = invocation.getArgument(0);
            ReflectionTestUtils.setField(invitation, "id", 77L);
            return invitation;
        });

        var response = projectInvitationService.create(
                4L,
                new ProjectInvitationCreateRequest(
                        "guest@example.com",
                        ProjectMemberRole.MEMBER,
                        "Tester",
                        "QA role",
                        true,
                        true,
                        false,
                        false,
                        false,
                        false
                ),
                "owner@example.com"
        );

        assertEquals(77L, response.id());
        assertEquals(ProjectInvitationStatus.PENDING, response.status());
        verify(activityLogService).log(eq(project), eq(actor), eq("Created invitation for guest@example.com"));
        verify(notificationService).notifyByEmail(
                eq("guest@example.com"),
                any(),
                eq("Project invitation"),
                eq("owner invited you to project Alpha"),
                eq(project),
                any(),
                eq(null)
        );
    }

    @Test
    void acceptShouldCreateMemberAndMarkInvitationAccepted() {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", 4L);
        project.setName("Alpha");

        User inviter = new User();
        ReflectionTestUtils.setField(inviter, "id", 1L);
        inviter.setUsername("owner");

        User actor = new User();
        ReflectionTestUtils.setField(actor, "id", 2L);
        actor.setEmail("guest@example.com");
        actor.setUsername("guest");

        ProjectInvitation invitation = new ProjectInvitation();
        ReflectionTestUtils.setField(invitation, "id", 77L);
        invitation.setProject(project);
        invitation.setInvitedBy(inviter);
        invitation.setInvitedEmail("guest@example.com");
        invitation.setToken("token-123");
        invitation.setStatus(ProjectInvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(1));
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setProposedRole(ProjectMemberRole.MEMBER);
        invitation.setRoleTitle("Tester");
        invitation.setRoleDescription("QA role");
        invitation.setCanViewProject(true);
        invitation.setCanCreateTask(true);
        invitation.setCanEditTask(false);
        invitation.setCanDeleteTask(false);
        invitation.setCanInviteMember(false);
        invitation.setCanManageMembers(false);

        when(projectInvitationRepository.findByToken("token-123")).thenReturn(Optional.of(invitation));
        when(userService.findEntityByEmail("guest@example.com")).thenReturn(actor);
        when(projectMemberRepository.existsByProjectAndUser(project, actor)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenAnswer(invocation -> {
            ProjectMember member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "id", 88L);
            return member;
        });
        when(projectInvitationRepository.save(any(ProjectInvitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = projectInvitationService.accept("token-123", "guest@example.com");

        assertEquals(ProjectInvitationStatus.ACCEPTED, response.status());
        verify(projectMemberRepository).save(any(ProjectMember.class));
        verify(activityLogService).log(eq(project), eq(actor), eq("Accepted invitation and joined as MEMBER"));
        verify(notificationService).notify(
                eq(inviter),
                any(),
                eq("Invitation accepted"),
                eq("guest accepted your invitation for Alpha"),
                eq(project),
                eq(invitation),
                any()
        );
    }
}
