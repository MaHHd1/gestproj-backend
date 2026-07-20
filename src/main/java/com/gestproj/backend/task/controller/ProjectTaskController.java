package com.gestproj.backend.task.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gestproj.backend.common.enums.TaskPriority;
import com.gestproj.backend.common.enums.TaskStatus;
import com.gestproj.backend.task.dto.TaskCreateRequest;
import com.gestproj.backend.task.dto.TaskResponse;
import com.gestproj.backend.task.service.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/projects/{projectId}/tasks")
public class ProjectTaskController {

  private final TaskService taskService;

  public ProjectTaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @PostMapping
  public ResponseEntity<TaskResponse> create(
      @PathVariable Long projectId,
      @Valid @RequestBody TaskCreateRequest request,
      Authentication authentication) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(taskService.create(projectId, request, authentication.getName()));
  }

  @GetMapping
  public ResponseEntity<Page<TaskResponse>> listByProject(
      @PathVariable Long projectId,
      Authentication authentication,
      @RequestParam(required = false) TaskStatus status,
      @RequestParam(required = false) TaskPriority priority,
      @RequestParam(required = false) Boolean overdue,
      @RequestParam(required = false) Boolean assignedToMe,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 20)
          Pageable pageable) {
    return ResponseEntity.ok(
        taskService.listByProject(
            projectId,
            authentication.getName(),
            status,
            priority,
            overdue,
            assignedToMe,
            pageable));
  }
}
