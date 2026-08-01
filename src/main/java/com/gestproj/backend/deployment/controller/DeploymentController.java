package com.gestproj.backend.deployment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestproj.backend.deployment.dto.DeploymentCreateRequest;
import com.gestproj.backend.deployment.dto.DeploymentResponse;
import com.gestproj.backend.deployment.service.DeploymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/projects/{projectId}/deployments")
public class DeploymentController {

  private final DeploymentService deploymentService;

  public DeploymentController(DeploymentService deploymentService) {
    this.deploymentService = deploymentService;
  }

  @PostMapping
  public ResponseEntity<DeploymentResponse> create(
      @PathVariable Long projectId,
      @Valid @RequestBody DeploymentCreateRequest request,
      Authentication authentication) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(deploymentService.recordDeployment(projectId, request, authentication.getName()));
  }

  @GetMapping
  public ResponseEntity<List<DeploymentResponse>> list(@PathVariable Long projectId, Authentication authentication) {
    return ResponseEntity.ok(deploymentService.getDeployments(projectId, authentication.getName()));
  }
}
