package com.gestproj.backend.task.controller;

import com.gestproj.backend.task.dto.TaskResponse;
import com.gestproj.backend.task.dto.TaskUpdateRequest;
import com.gestproj.backend.task.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long taskId, Authentication authentication) {
        return ResponseEntity.ok(taskService.getById(taskId, authentication.getName()));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long taskId,
            @RequestBody TaskUpdateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(taskService.update(taskId, request, authentication.getName()));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(@PathVariable Long taskId, Authentication authentication) {
        taskService.delete(taskId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
