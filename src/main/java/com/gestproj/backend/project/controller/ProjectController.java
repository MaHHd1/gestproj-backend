package com.gestproj.backend.project.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestproj.backend.project.dto.ProjectCreateRequest;
import com.gestproj.backend.project.dto.ProjectResponse;
import com.gestproj.backend.project.dto.ProjectStatisticsResponse;
import com.gestproj.backend.project.dto.ProjectUpdateRequest;
import com.gestproj.backend.project.service.ProjectService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/projects")
public class ProjectController {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @PostMapping
  public ResponseEntity<ProjectResponse> create(
      @Valid @RequestBody ProjectCreateRequest request, Authentication authentication) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(projectService.create(request, authentication.getName()));
  }

  @GetMapping
  public ResponseEntity<List<ProjectResponse>> list(Authentication authentication) {
    return ResponseEntity.ok(projectService.getProjectsForCurrentUser(authentication.getName()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProjectResponse> getById(
      @PathVariable Long id, Authentication authentication) {
    return ResponseEntity.ok(projectService.getById(id, authentication.getName()));
  }

  @GetMapping("/{id}/statistics")
  public ResponseEntity<ProjectStatisticsResponse> getStatistics(
      @PathVariable Long id, Authentication authentication) {
    return ResponseEntity.ok(projectService.getStatistics(id, authentication.getName()));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProjectResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody ProjectUpdateRequest request,
      Authentication authentication) {
    return ResponseEntity.ok(projectService.update(id, request, authentication.getName()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
    projectService.delete(id, authentication.getName());
    return ResponseEntity.noContent().build();
  }
}
