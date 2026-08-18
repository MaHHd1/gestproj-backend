package com.gestproj.backend.workflow.dto;

import java.time.OffsetDateTime;
import java.util.List;

/** A job inside a Gitea Actions workflow run. */
public record WorkflowJobResponse(
    Long id,
    String name,
    String status,
    String conclusion,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt,
    String htmlUrl,
    List<WorkflowStepResponse> steps) {}
