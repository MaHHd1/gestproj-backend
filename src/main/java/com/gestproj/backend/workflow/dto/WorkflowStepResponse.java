package com.gestproj.backend.workflow.dto;

import java.time.OffsetDateTime;

/** An individual step executed by a Gitea Actions job. */
public record WorkflowStepResponse(
    Integer number,
    String name,
    String status,
    String conclusion,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt) {}
