package com.gestproj.backend.deployment.dto;

import java.time.LocalDateTime;

public record DeploymentResponse(
    Long id,
    Long projectId,
    String status,
    String commitHash,
    String commitMessage,
    String triggeredBy,
    LocalDateTime startedAt,
    LocalDateTime finishedAt,
    String workflowName,
    String workflowRunId,
    String workflowUrl,
    String deploymentTarget,
    String dockerStatus,
    String dockerDetails) {}
