package com.gestproj.backend.task.service;

import com.gestproj.backend.activitylog.service.ActivityLogService;
import com.gestproj.backend.common.enums.ProjectMemberRole;
import com.gestproj.backend.common.enums.TaskPriority;
import com.gestproj.backend.common.enums.TaskStatus;
import com.gestproj.backend.common.exception.ForbiddenException;
import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.member.service.ProjectMemberService;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.project.repository.ProjectRepository;
import com.gestproj.backend.task.dto.TaskCreateRequest;
import com.gestproj.backend.task.dto.TaskUpdateRequest;
import com.gestproj.backend.task.entity.Task;
import com.gestproj.backend.task.repository.TaskRepository;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.repository.UserRepository;
import com.gestproj.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberService projectMemberService;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createShouldPersistTaskWhenMemberCanCreate() {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", 3L);
        project.setName("Alpha");

        User actor = new User();
        ReflectionTestUtils.setField(actor, "id", 1L);
        actor.setEmail("actor@example.com");

        ProjectMember actorMember = new ProjectMember();
        actorMember.setProject(project);
        actorMember.setUser(actor);
        actorMember.setRole(ProjectMemberRole.MEMBER);
        actorMember.setCanViewProject(true);
        actorMember.setCanCreateTask(true);

        User assigned = new User();
        ReflectionTestUtils.setField(assigned, "id", 2L);
        assigned.setUsername("assigned");

        when(projectRepository.findById(3L)).thenReturn(Optional.of(project));
        when(userService.findEntityByEmail("actor@example.com")).thenReturn(actor);
        when(projectMemberService.findProjectMember(project, actor)).thenReturn(actorMember);
        when(projectMemberService.isMember(project, assigned)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(assigned));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            ReflectionTestUtils.setField(task, "id", 99L);
            return task;
        });

        var response = taskService.create(
                3L,
                new TaskCreateRequest("Task 1", "Description", TaskStatus.EN_COURS, TaskPriority.HAUTE, LocalDate.now().plusDays(2), 2L),
                "actor@example.com"
        );

        assertEquals(99L, response.id());
        assertEquals("Task 1", response.title());
        assertEquals(2L, response.assignedTo());
        assertEquals("assigned", response.assignedToUsername());
        verify(activityLogService).log(eq(project), eq(actor), eq("Created task Task 1"));
    }

    @Test
    void updateShouldRejectWhenNoEditPermission() {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", 3L);

        User actor = new User();
        ReflectionTestUtils.setField(actor, "id", 1L);
        actor.setEmail("actor@example.com");

        ProjectMember actorMember = new ProjectMember();
        actorMember.setProject(project);
        actorMember.setUser(actor);
        actorMember.setRole(ProjectMemberRole.MEMBER);
        actorMember.setCanEditTask(false);
        actorMember.setCanViewProject(true);

        Task task = new Task();
        task.setProject(project);
        task.setTitle("Task 1");
        ReflectionTestUtils.setField(task, "id", 50L);

        when(taskRepository.findById(50L)).thenReturn(Optional.of(task));
        when(userService.findEntityByEmail("actor@example.com")).thenReturn(actor);
        when(projectMemberService.findProjectMember(project, actor)).thenReturn(actorMember);

        assertThrows(ForbiddenException.class, () ->
                taskService.update(50L, new TaskUpdateRequest("Updated", null, null, null, null, null), "actor@example.com")
        );
    }

    @Test
    void listByProjectShouldReturnPagedTasks() {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", 3L);

        User actor = new User();
        ReflectionTestUtils.setField(actor, "id", 1L);
        actor.setEmail("actor@example.com");

        ProjectMember actorMember = new ProjectMember();
        actorMember.setProject(project);
        actorMember.setUser(actor);
        actorMember.setRole(ProjectMemberRole.MEMBER);
        actorMember.setCanViewProject(true);

        Task task = new Task();
        task.setProject(project);
        task.setTitle("Task 1");
        task.setStatus(TaskStatus.A_FAIRE);
        task.setPriority(TaskPriority.MOYENNE);
        task.setDueDate(LocalDate.now().plusDays(1));
        ReflectionTestUtils.setField(task, "id", 10L);

        when(projectRepository.findById(3L)).thenReturn(Optional.of(project));
        when(userService.findEntityByEmail("actor@example.com")).thenReturn(actor);
        when(projectMemberService.findProjectMember(project, actor)).thenReturn(actorMember);
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(task)));

        var response = taskService.listByProject(
                3L,
                "actor@example.com",
                TaskStatus.A_FAIRE,
                TaskPriority.MOYENNE,
                true,
                false,
                PageRequest.of(0, 10)
        );

        assertEquals(1, response.getTotalElements());
        assertEquals("Task 1", response.getContent().get(0).title());
        verify(taskRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}
