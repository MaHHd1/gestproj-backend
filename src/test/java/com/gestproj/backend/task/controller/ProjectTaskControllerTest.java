package com.gestproj.backend.task.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import com.gestproj.backend.common.enums.TaskPriority;
import com.gestproj.backend.common.enums.TaskStatus;
import com.gestproj.backend.task.dto.TaskCreateRequest;
import com.gestproj.backend.task.dto.TaskResponse;
import com.gestproj.backend.task.service.TaskService;

@ExtendWith(MockitoExtension.class)
class ProjectTaskControllerTest {

  @Mock private TaskService taskService;

  @Mock private Authentication authentication;

  @InjectMocks private ProjectTaskController projectTaskController;

  @Test
  void createShouldReturnCreatedTask() {
    when(authentication.getName()).thenReturn("owner@example.com");
    when(taskService.create(eq(1L), any(TaskCreateRequest.class), eq("owner@example.com")))
        .thenReturn(
            new TaskResponse(
                9L,
                1L,
                "Task 1",
                "Description",
                TaskStatus.A_FAIRE,
                TaskPriority.MOYENNE,
                LocalDate.now(),
                false,
                null,
                null));

    var response =
        projectTaskController.create(
            1L,
            new TaskCreateRequest(
                "Task 1",
                "Description",
                TaskStatus.A_FAIRE,
                TaskPriority.MOYENNE,
                LocalDate.now(),
                null),
            authentication);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals("Task 1", response.getBody().title());
  }

  @Test
  void listShouldReturnTasks() {
    when(authentication.getName()).thenReturn("owner@example.com");
    when(taskService.listByProject(
            eq(1L),
            eq("owner@example.com"),
            nullable(TaskStatus.class),
            nullable(TaskPriority.class),
            nullable(Boolean.class),
            nullable(Boolean.class),
            any(Pageable.class)))
        .thenReturn(
            new PageImpl<>(
                List.of(
                    new TaskResponse(
                        9L,
                        1L,
                        "Task 1",
                        "Description",
                        TaskStatus.A_FAIRE,
                        TaskPriority.MOYENNE,
                        LocalDate.now(),
                        false,
                        null,
                        null))));

    var response =
        projectTaskController.listByProject(
            1L, authentication, null, null, null, null, PageRequest.of(0, 20));

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().getContent().size());
  }
}
