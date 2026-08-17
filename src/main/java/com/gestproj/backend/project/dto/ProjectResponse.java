package com.gestproj.backend.project.dto;

public record ProjectResponse(
    Long id,
    String name,
    String description,
    Long ownerId,
    String ownerUsername,
    String repoOwner,
    String repoName,
    String deploymentHost,
    String deploymentContainer) {
  public ProjectResponse(
      Long id,
      String name,
      String description,
      Long ownerId,
      String ownerUsername,
      String repoOwner,
      String repoName) {
    this(id, name, description, ownerId, ownerUsername, repoOwner, repoName, null, null);
  }
}
