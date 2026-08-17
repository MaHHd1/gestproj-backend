package com.gestproj.backend.workflow.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestproj.backend.workflow.dto.WorkflowJobResponse;
import com.gestproj.backend.workflow.dto.WorkflowRunResponse;
import com.gestproj.backend.workflow.service.WorkflowService;

@RestController
@RequestMapping("/api/projects/{projectId}/workflows")
public class WorkflowController {
  private final WorkflowService workflowService;

  public WorkflowController(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @GetMapping("/runs")
  public ResponseEntity<List<WorkflowRunResponse>> runs(
      @PathVariable Long projectId, Authentication auth) {
    return ResponseEntity.ok(workflowService.getRuns(projectId, auth.getName()));
  }

  @GetMapping("/runs/{runId}/jobs")
  public ResponseEntity<List<WorkflowJobResponse>> jobs(
      @PathVariable Long projectId, @PathVariable Long runId, Authentication auth) {
    return ResponseEntity.ok(workflowService.getJobs(projectId, runId, auth.getName()));
  }

  @GetMapping(value = "/runs/{runId}/jobs/{jobId}/logs", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> logs(
      @PathVariable Long projectId,
      @PathVariable Long runId,
      @PathVariable Long jobId,
      Authentication auth) {
    return ResponseEntity.ok(workflowService.getJobLogs(projectId, runId, jobId, auth.getName()));
  }
}
