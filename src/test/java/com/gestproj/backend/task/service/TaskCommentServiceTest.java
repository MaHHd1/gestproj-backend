package com.gestproj.backend.task.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.gestproj.backend.common.exception.ResourceNotFoundException;
import com.gestproj.backend.member.service.ProjectMemberService;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.task.entity.Task;
import com.gestproj.backend.task.entity.TaskComment;
import com.gestproj.backend.task.repository.TaskCommentRepository;
import com.gestproj.backend.task.repository.TaskRepository;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class TaskCommentServiceTest {

  @Mock private TaskCommentRepository taskCommentRepository;

  @Mock private TaskRepository taskRepository;

  @Mock private UserService userService;

  @Mock private ProjectMemberService projectMemberService;

  @InjectMocks private TaskCommentService taskCommentService;

  @Test
  void deleteShouldRejectCommentFromDifferentTask() {
    User author = new User();
    ReflectionTestUtils.setField(author, "id", 1L);

    Project project = new Project();
    ReflectionTestUtils.setField(project, "id", 2L);

    Task actualTask = new Task();
    ReflectionTestUtils.setField(actualTask, "id", 10L);
    actualTask.setProject(project);

    TaskComment comment = new TaskComment();
    ReflectionTestUtils.setField(comment, "id", 99L);
    comment.setTask(actualTask);
    comment.setUser(author);

    when(userService.findEntityByEmail("author@example.com")).thenReturn(author);
    when(taskCommentRepository.findById(99L)).thenReturn(Optional.of(comment));

    assertThrows(
        ResourceNotFoundException.class,
        () -> taskCommentService.delete(11L, 99L, "author@example.com"));
    verify(taskCommentRepository, never()).delete(comment);
  }
}
