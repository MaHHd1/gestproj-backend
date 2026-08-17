package com.gestproj.backend.deployment.entity;

import java.time.LocalDateTime;

import com.gestproj.backend.project.entity.Project;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "deployments")
@SuppressFBWarnings("EI_EXPOSE_REP")
public class Deployment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Column(nullable = false)
  private String status;

  @Column(name = "commit_hash")
  private String commitHash;

  @Column(name = "commit_message", columnDefinition = "text")
  private String commitMessage;

  @Column(name = "triggered_by")
  private String triggeredBy;

  @Column(name = "started_at")
  private LocalDateTime startedAt;

  @Column(name = "finished_at")
  private LocalDateTime finishedAt;

  @Column(name = "workflow_name")
  private String workflowName;

  @Column(name = "workflow_run_id")
  private String workflowRunId;

  @Column(name = "workflow_url", length = 1000)
  private String workflowUrl;

  @Column(name = "deployment_target")
  private String deploymentTarget;

  @Column(name = "docker_status")
  private String dockerStatus;

  @Column(name = "docker_details", columnDefinition = "text")
  private String dockerDetails;

  public Long getId() {
    return id;
  }

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Project getProject() {
    return project;
  }

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public void setProject(Project project) {
    this.project = project;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getCommitHash() {
    return commitHash;
  }

  public void setCommitHash(String commitHash) {
    this.commitHash = commitHash;
  }

  public String getCommitMessage() {
    return commitMessage;
  }

  public void setCommitMessage(String commitMessage) {
    this.commitMessage = commitMessage;
  }

  public String getTriggeredBy() {
    return triggeredBy;
  }

  public void setTriggeredBy(String triggeredBy) {
    this.triggeredBy = triggeredBy;
  }

  public LocalDateTime getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(LocalDateTime startedAt) {
    this.startedAt = startedAt;
  }

  public LocalDateTime getFinishedAt() {
    return finishedAt;
  }

  public void setFinishedAt(LocalDateTime finishedAt) {
    this.finishedAt = finishedAt;
  }

  public String getWorkflowName() {
    return workflowName;
  }

  public void setWorkflowName(String workflowName) {
    this.workflowName = workflowName;
  }

  public String getWorkflowRunId() {
    return workflowRunId;
  }

  public void setWorkflowRunId(String workflowRunId) {
    this.workflowRunId = workflowRunId;
  }

  public String getWorkflowUrl() {
    return workflowUrl;
  }

  public void setWorkflowUrl(String workflowUrl) {
    this.workflowUrl = workflowUrl;
  }

  public String getDeploymentTarget() {
    return deploymentTarget;
  }

  public void setDeploymentTarget(String deploymentTarget) {
    this.deploymentTarget = deploymentTarget;
  }

  public String getDockerStatus() {
    return dockerStatus;
  }

  public void setDockerStatus(String dockerStatus) {
    this.dockerStatus = dockerStatus;
  }

  public String getDockerDetails() {
    return dockerDetails;
  }

  public void setDockerDetails(String dockerDetails) {
    this.dockerDetails = dockerDetails;
  }
}
