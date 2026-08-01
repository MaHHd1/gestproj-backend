package com.gestproj.backend.project.dto;

import java.time.OffsetDateTime;

public record CommitResponse(String sha, String message, String authorName, OffsetDateTime date) {}
