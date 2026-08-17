package com.gestproj.backend.workflow.dto;

import java.time.OffsetDateTime;

/** A job inside a Gitea Actions workflow run. */
public record WorkflowJobResponse(
    Long id,
    String name,
    String status,
    String conclusion,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt,
    String htmlUrl) {}
