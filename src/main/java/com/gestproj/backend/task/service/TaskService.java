package com.gestproj.backend.task.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.gestproj.backend.activitylog.service.ActivityLogService;
import com.gestproj.backend.common.enums.TaskPriority;
import com.gestproj.backend.common.enums.TaskStatus;
import com.gestproj.backend.common.exception.ForbiddenException;
import com.gestproj.backend.common.exception.ResourceNotFoundException;
import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.member.service.ProjectMemberService;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.project.repository.ProjectRepository;
import com.gestproj.backend.task.dto.TaskCreateRequest;
import com.gestproj.backend.task.dto.TaskResponse;
import com.gestproj.backend.task.dto.TaskUpdateRequest;
import com.gestproj.backend.task.entity.Task;
import com.gestproj.backend.task.repository.TaskRepository;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.repository.UserRepository;
import com.gestproj.backend.user.service.UserService;

@Service
public class TaskService {

  private final TaskRepository taskRepository;
  private final ProjectRepository projectRepository;
  private final ProjectMemberService projectMemberService;
  private final ActivityLogService activityLogService;
  private final UserService userService;
  private final UserRepository userRepository;

  public TaskService(
      TaskRepository taskRepository,
      ProjectRepository projectRepository,
      ProjectMemberService projectMemberService,
      ActivityLogService activityLogService,
      UserService userService,
      UserRepository userRepository) {
    this.taskRepository = taskRepository;
    this.projectRepository = projectRepository;
    this.projectMemberService = projectMemberService;
    this.activityLogService = activityLogService;
    this.userService = userService;
    this.userRepository = userRepository;
  }

  public TaskResponse create(Long projectId, TaskCreateRequest request, String actorEmail) {
    Project project = getAccessibleProject(projectId, actorEmail, true);
    ProjectMember actorMember = getMember(project, actorEmail);

    if (!actorMember.isCanCreateTask()
        && actorMember.getRole() != com.gestproj.backend.common.enums.ProjectMemberRole.OWNER) {
      throw new ForbiddenException("You are not allowed to create tasks in this project");
    }

    User assignedUser = resolveAssignedUser(project, request.assignedTo());

    Task task = new Task();
    task.setProject(project);
    task.setTitle(request.title().trim());
    task.setDescription(request.description() == null ? null : request.description().trim());
    task.setStatus(request.status() == null ? TaskStatus.A_FAIRE : request.status());
    task.setPriority(request.priority() == null ? TaskPriority.MOYENNE : request.priority());
    task.setDueDate(request.dueDate());
    task.setAssignedTo(assignedUser);
    task.setLate(computeLate(task.getDueDate(), task.getStatus()));
    task.setCreatedAt(LocalDateTime.now());
    task.setUpdatedAt(LocalDateTime.now());

    Task savedTask = taskRepository.save(task);
    activityLogService.log(
        project, userService.findEntityByEmail(actorEmail), "Created task " + savedTask.getTitle());
    return toResponse(savedTask);
  }

  public Page<TaskResponse> listByProject(
      Long projectId,
      String actorEmail,
      TaskStatus status,
      TaskPriority priority,
      Boolean overdue,
      Boolean assignedToMe,
      Pageable pageable) {
    Project project = getAccessibleProject(projectId, actorEmail, false);
    User actor = userService.findEntityByEmail(actorEmail);

    Specification<Task> specification = Specification.where(hasProject(project));
    if (status != null) {
      specification = specification.and((root, query, cb) -> cb.equal(root.get("status"), status));
    }
    if (priority != null) {
      specification =
          specification.and((root, query, cb) -> cb.equal(root.get("priority"), priority));
    }
    if (Boolean.TRUE.equals(overdue)) {
      specification = specification.and((root, query, cb) -> cb.isTrue(root.get("late")));
    }
    if (Boolean.TRUE.equals(assignedToMe)) {
      specification =
          specification.and(
              (root, query, cb) -> cb.equal(root.get("assignedTo").get("id"), actor.getId()));
    }

    return taskRepository.findAll(specification, pageable).map(this::toResponse);
  }

  public TaskResponse getById(Long taskId, String actorEmail) {
    Task task = getTask(taskId);
    getAccessibleProject(task.getProject().getId(), actorEmail, false);
    return toResponse(task);
  }

  public TaskResponse update(Long taskId, TaskUpdateRequest request, String actorEmail) {
    Task task = getTask(taskId);
    Project project = task.getProject();
    ProjectMember actorMember = getMember(project, actorEmail);

    if (!actorMember.isCanEditTask()
        && actorMember.getRole() != com.gestproj.backend.common.enums.ProjectMemberRole.OWNER) {
      throw new ForbiddenException("You are not allowed to edit tasks in this project");
    }

    if (request.title() != null) {
      task.setTitle(request.title().trim());
    }
    if (request.description() != null) {
      task.setDescription(request.description().trim());
    }
    if (request.status() != null) {
      task.setStatus(request.status());
    }
    if (request.priority() != null) {
      task.setPriority(request.priority());
    }
    if (request.dueDate() != null) {
      task.setDueDate(request.dueDate());
    }
    if (request.assignedTo() != null) {
      task.setAssignedTo(resolveAssignedUser(project, request.assignedTo()));
    }
    task.setLate(computeLate(task.getDueDate(), task.getStatus()));
    task.setUpdatedAt(LocalDateTime.now());

    Task savedTask = taskRepository.save(task);
    activityLogService.log(
        project, userService.findEntityByEmail(actorEmail), "Updated task " + savedTask.getTitle());
    return toResponse(savedTask);
  }

  public void delete(Long taskId, String actorEmail) {
    Task task = getTask(taskId);
    ProjectMember actorMember = getMember(task.getProject(), actorEmail);

    if (!actorMember.isCanDeleteTask()
        && actorMember.getRole() != com.gestproj.backend.common.enums.ProjectMemberRole.OWNER) {
      throw new ForbiddenException("You are not allowed to delete tasks in this project");
    }

    activityLogService.log(
        task.getProject(),
        userService.findEntityByEmail(actorEmail),
        "Deleted task " + task.getTitle());
    taskRepository.delete(task);
  }

  private Task getTask(Long taskId) {
    return taskRepository
        .findById(taskId)
        .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
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
      throw new ForbiddenException("You are not allowed to create tasks in this project");
    }
    return project;
  }

  private ProjectMember getMember(Project project, String actorEmail) {
    User actor = userService.findEntityByEmail(actorEmail);
    return projectMemberService.findProjectMember(project, actor);
  }

  private User resolveAssignedUser(Project project, Long assignedToId) {
    if (assignedToId == null) {
      return null;
    }
    User assignedUser =
        userRepository
            .findById(assignedToId)
            .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found"));
    if (!projectMemberService.isMember(project, assignedUser)) {
      throw new ForbiddenException("Assigned user must be a project member");
    }
    return assignedUser;
  }

  private boolean computeLate(LocalDate dueDate, TaskStatus status) {
    return dueDate != null && status != TaskStatus.TERMINE && dueDate.isBefore(LocalDate.now());
  }

  private Specification<Task> hasProject(Project project) {
    return (root, query, cb) -> cb.equal(root.get("project").get("id"), project.getId());
  }

  private TaskResponse toResponse(Task task) {
    return new TaskResponse(
        task.getId(),
        task.getProject().getId(),
        task.getTitle(),
        task.getDescription(),
        task.getStatus(),
        task.getPriority(),
        task.getDueDate(),
        task.isLate(),
        task.getAssignedTo() == null ? null : task.getAssignedTo().getId(),
        task.getAssignedTo() == null ? null : task.getAssignedTo().getUsername());
  }
}
