package com.gestproj.backend.deployment.dto;

public record DeploymentCreateRequest(
    String status,
    String commitHash,
    String commitMessage,
    String triggeredBy,
    String workflowName,
    String workflowRunId,
    String workflowUrl,
    String deploymentTarget,
    String dockerStatus,
    String dockerDetails) {
  public DeploymentCreateRequest(
      String status, String commitHash, String commitMessage, String triggeredBy) {
    this(status, commitHash, commitMessage, triggeredBy, null, null, null, null, null, null);
  }
}
