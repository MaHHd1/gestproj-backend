package com.gestproj.backend.deployment.dto;

public record DeploymentCreateRequest(String status, String commitHash, String commitMessage, String triggeredBy) {}
