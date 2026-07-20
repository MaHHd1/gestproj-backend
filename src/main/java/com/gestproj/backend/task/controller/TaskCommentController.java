package com.gestproj.backend.task.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestproj.backend.task.dto.TaskCommentCreateRequest;
import com.gestproj.backend.task.dto.TaskCommentResponse;
import com.gestproj.backend.task.service.TaskCommentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tasks/{taskId}/comments")
public class TaskCommentController {

  private final TaskCommentService taskCommentService;

  public TaskCommentController(TaskCommentService taskCommentService) {
    this.taskCommentService = taskCommentService;
  }

  @PostMapping
  public ResponseEntity<TaskCommentResponse> create(
      @PathVariable Long taskId,
      @Valid @RequestBody TaskCommentCreateRequest request,
      Authentication authentication) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(taskCommentService.create(taskId, request, authentication.getName()));
  }

  @GetMapping
  public ResponseEntity<List<TaskCommentResponse>> getByTask(
      @PathVariable Long taskId, Authentication authentication) {
    return ResponseEntity.ok(taskCommentService.getByTaskId(taskId, authentication.getName()));
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<TaskCommentResponse> delete(
      @PathVariable Long taskId, @PathVariable Long commentId, Authentication authentication) {
    return ResponseEntity.ok(
        taskCommentService.delete(taskId, commentId, authentication.getName()));
  }
}
