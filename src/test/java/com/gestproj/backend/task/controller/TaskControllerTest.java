package com.gestproj.backend.task.controller;

import com.gestproj.backend.common.enums.TaskPriority;
import com.gestproj.backend.common.enums.TaskStatus;
import com.gestproj.backend.task.dto.TaskResponse;
import com.gestproj.backend.task.dto.TaskUpdateRequest;
import com.gestproj.backend.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskController taskController;

    @Test
    void getByIdShouldReturnTask() {
        when(authentication.getName()).thenReturn("owner@example.com");
        when(taskService.getById(5L, "owner@example.com")).thenReturn(
                new TaskResponse(5L, 1L, "Task", "Desc", TaskStatus.EN_COURS, TaskPriority.HAUTE, LocalDate.now(), false, 2L, "assignee")
        );

        var response = taskController.getById(5L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Task", response.getBody().title());
    }

    @Test
    void updateShouldReturnUpdatedTask() {
        when(authentication.getName()).thenReturn("owner@example.com");
        when(taskService.update(eq(5L), any(TaskUpdateRequest.class), eq("owner@example.com"))).thenReturn(
                new TaskResponse(5L, 1L, "Task Updated", "Desc", TaskStatus.EN_COURS, TaskPriority.HAUTE, LocalDate.now(), false, 2L, "assignee")
        );

        var response = taskController.update(5L, new TaskUpdateRequest("Task Updated", "Desc", TaskStatus.EN_COURS, TaskPriority.HAUTE, LocalDate.now(), 2L), authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Task Updated", response.getBody().title());
    }

    @Test
    void deleteShouldReturnNoContent() {
        when(authentication.getName()).thenReturn("owner@example.com");

        var response = taskController.delete(5L, authentication);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
