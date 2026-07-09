package com.gestproj.backend.projectinvitation.service;

import com.gestproj.backend.common.enums.ProjectInvitationStatus;
import com.gestproj.backend.common.enums.ProjectMemberRole;
import com.gestproj.backend.common.enums.ProjectMemberStatus;
import com.gestproj.backend.common.enums.NotificationType;
import com.gestproj.backend.common.exception.ConflictException;
import com.gestproj.backend.common.exception.ForbiddenException;
import com.gestproj.backend.common.exception.ResourceNotFoundException;
import com.gestproj.backend.common.mail.EmailService;
import com.gestproj.backend.activitylog.service.ActivityLogService;
import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.member.repository.ProjectMemberRepository;
import com.gestproj.backend.member.service.ProjectMemberService;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.project.repository.ProjectRepository;
import com.gestproj.backend.projectinvitation.dto.ProjectInvitationCreateRequest;
import com.gestproj.backend.projectinvitation.dto.ProjectInvitationResponse;
import com.gestproj.backend.projectinvitation.entity.ProjectInvitation;
import com.gestproj.backend.projectinvitation.repository.ProjectInvitationRepository;
import com.gestproj.backend.notification.service.NotificationService;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.repository.UserRepository;
import com.gestproj.backend.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ProjectInvitationService {

    private static final int INVITE_EXPIRATION_DAYS = 7;

    private final ProjectInvitationRepository projectInvitationRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMemberService projectMemberService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public ProjectInvitationService(
            ProjectInvitationRepository projectInvitationRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectMemberService projectMemberService,
            NotificationService notificationService,
            ActivityLogService activityLogService,
            UserService userService,
            UserRepository userRepository,
            EmailService emailService
    ) {
        this.projectInvitationRepository = projectInvitationRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectMemberService = projectMemberService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public ProjectInvitationResponse create(Long projectId, ProjectInvitationCreateRequest request, String actorEmail) {
        Project project = getProject(projectId);
        User actor = userService.findEntityByEmail(actorEmail);
        ProjectMember actorMember = projectMemberService.findProjectMember(project, actor);

        if (actorMember.getRole() != ProjectMemberRole.OWNER && !actorMember.isCanInviteMember()) {
            throw new ForbiddenException("You are not allowed to invite members");
        }

        String invitedEmail = request.invitedEmail() == null ? null : request.invitedEmail().trim().toLowerCase(Locale.ROOT);
        if (invitedEmail != null && userRepository.findByEmail(invitedEmail).isPresent()) {
            User invitedUser = userRepository.findByEmail(invitedEmail).orElseThrow();
            if (projectMemberRepository.existsByProjectAndUser(project, invitedUser)) {
                throw new ConflictException("This user is already a project member");
            }
        }

        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setProject(project);
        invitation.setInvitedBy(actor);
        invitation.setInvitedEmail(invitedEmail);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setStatus(ProjectInvitationStatus.PENDING);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(INVITE_EXPIRATION_DAYS));
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setProposedRole(request.proposedRole() == null ? ProjectMemberRole.MEMBER : request.proposedRole());
        invitation.setRoleTitle(request.roleTitle() == null ? "Member" : request.roleTitle().trim());
        invitation.setRoleDescription(request.roleDescription() == null ? "Project member" : request.roleDescription().trim());
        invitation.setCanViewProject(request.canViewProject());
        invitation.setCanCreateTask(request.canCreateTask());
        invitation.setCanEditTask(request.canEditTask());
        invitation.setCanDeleteTask(request.canDeleteTask());
        invitation.setCanInviteMember(request.canInviteMember());
        invitation.setCanManageMembers(request.canManageMembers());

        ProjectInvitation savedInvitation = projectInvitationRepository.save(invitation);
        activityLogService.log(project, actor, "Created invitation for " + (invitedEmail == null ? "link" : invitedEmail));
        if (invitedEmail != null && userRepository.findByEmail(invitedEmail).isPresent()) {
            notificationService.notifyByEmail(
                    invitedEmail,
                    NotificationType.INVITATION_SENT,
                    "Project invitation",
                    actor.getUsername() + " invited you to project " + project.getName(),
                    project,
                    savedInvitation,
                    null
            );
        }
        emailService.sendProjectInvitationEmail(savedInvitation);

        return toResponse(savedInvitation);
    }

    public List<ProjectInvitationResponse> listForProject(Long projectId, String actorEmail) {
        Project project = getProject(projectId);
        User actor = userService.findEntityByEmail(actorEmail);
        projectMemberService.findProjectMember(project, actor);

        return projectInvitationRepository.findAllByProjectOrderByCreatedAtDesc(project)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProjectInvitationResponse accept(String token, String actorEmail) {
        ProjectInvitation invitation = getActiveInvitation(token);
        User actor = userService.findEntityByEmail(actorEmail);

        if (invitation.getInvitedEmail() != null && !invitation.getInvitedEmail().equalsIgnoreCase(actor.getEmail())) {
            throw new ForbiddenException("This invitation is not for your account");
        }

        Project project = invitation.getProject();
        if (projectMemberRepository.existsByProjectAndUser(project, actor)) {
            invitation.setStatus(ProjectInvitationStatus.ACCEPTED);
            ProjectInvitation savedInvitation = projectInvitationRepository.save(invitation);
            activityLogService.log(project, actor, "Accepted invitation");
            notificationService.notify(
                    invitation.getInvitedBy(),
                    NotificationType.INVITATION_ACCEPTED,
                    "Invitation accepted",
                    actor.getUsername() + " accepted your invitation for " + project.getName(),
                    project,
                    savedInvitation,
                    null
            );
            return toResponse(savedInvitation);
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(actor);
        member.setRole(invitation.getProposedRole());
        member.setStatus(ProjectMemberStatus.ACTIVE);
        member.setRoleTitle(invitation.getRoleTitle());
        member.setRoleDescription(invitation.getRoleDescription());
        member.setCanViewProject(invitation.isCanViewProject());
        member.setCanCreateTask(invitation.isCanCreateTask());
        member.setCanEditTask(invitation.isCanEditTask());
        member.setCanDeleteTask(invitation.isCanDeleteTask());
        member.setCanInviteMember(invitation.isCanInviteMember());
        member.setCanManageMembers(invitation.isCanManageMembers());
        ProjectMember savedMember = projectMemberRepository.save(member);

        invitation.setStatus(ProjectInvitationStatus.ACCEPTED);
        ProjectInvitation savedInvitation = projectInvitationRepository.save(invitation);
        activityLogService.log(project, actor, "Accepted invitation and joined as " + savedMember.getRole());
        notificationService.notify(
                invitation.getInvitedBy(),
                NotificationType.INVITATION_ACCEPTED,
                "Invitation accepted",
                actor.getUsername() + " accepted your invitation for " + project.getName(),
                project,
                savedInvitation,
                savedMember
        );
        return toResponse(savedInvitation);
    }

    @Transactional
    public ProjectInvitationResponse reject(String token, String actorEmail) {
        ProjectInvitation invitation = getActiveInvitation(token);
        User actor = userService.findEntityByEmail(actorEmail);

        if (invitation.getInvitedEmail() != null && !invitation.getInvitedEmail().equalsIgnoreCase(actor.getEmail())) {
            throw new ForbiddenException("This invitation is not for your account");
        }

        invitation.setStatus(ProjectInvitationStatus.REJECTED);
        ProjectInvitation savedInvitation = projectInvitationRepository.save(invitation);
        activityLogService.log(invitation.getProject(), actor, "Rejected invitation");
        notificationService.notify(
                invitation.getInvitedBy(),
                NotificationType.INVITATION_REJECTED,
                "Invitation rejected",
                actor.getUsername() + " rejected your invitation for " + invitation.getProject().getName(),
                invitation.getProject(),
                savedInvitation,
                null
        );
        return toResponse(savedInvitation);
    }

    public ProjectInvitationResponse getByToken(String token, String actorEmail) {
        ProjectInvitation invitation = getInvitation(token);
        User actor = userService.findEntityByEmail(actorEmail);
        ProjectMember actorMember = projectMemberService.findProjectMember(invitation.getProject(), actor);

        if (actorMember == null) {
            throw new ForbiddenException("You are not allowed to view this invitation");
        }

        return toResponse(invitation);
    }

    private Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private ProjectInvitation getInvitation(String token) {
        return projectInvitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
    }

    private ProjectInvitation getActiveInvitation(String token) {
        ProjectInvitation invitation = getInvitation(token);
        if (invitation.getStatus() != ProjectInvitationStatus.PENDING) {
            throw new ConflictException("Invitation is no longer pending");
        }
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(ProjectInvitationStatus.EXPIRED);
            projectInvitationRepository.save(invitation);
            throw new ConflictException("Invitation has expired");
        }
        return invitation;
    }

    private ProjectInvitationResponse toResponse(ProjectInvitation invitation) {
        return new ProjectInvitationResponse(
                invitation.getId(),
                invitation.getProject().getId(),
                invitation.getInvitedBy().getId(),
                invitation.getInvitedEmail(),
                invitation.getToken(),
                invitation.getStatus(),
                invitation.getExpiresAt(),
                invitation.getProposedRole(),
                invitation.getRoleTitle(),
                invitation.getRoleDescription(),
                invitation.isCanViewProject(),
                invitation.isCanCreateTask(),
                invitation.isCanEditTask(),
                invitation.isCanDeleteTask(),
                invitation.isCanInviteMember(),
                invitation.isCanManageMembers()
        );
    }
}
