package com.gestproj.backend.workflow.dto;

import java.time.OffsetDateTime;

/** A Gitea Actions workflow run, normalized for the dashboard. */
public record WorkflowRunResponse(
    Long id,
    String name,
    String status,
    String conclusion,
    String commitHash,
    String commitMessage,
    String author,
    String branch,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    String htmlUrl) {}
