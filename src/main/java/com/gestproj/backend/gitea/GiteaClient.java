package com.gestproj.backend.gitea;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestproj.backend.project.dto.CommitResponse;
import com.gestproj.backend.workflow.dto.WorkflowJobResponse;
import com.gestproj.backend.workflow.dto.WorkflowRunResponse;

@Service
@SuppressWarnings({"PMD.ControlStatementBraces", "PMD.GuardLogStatement"})
public class GiteaClient {

  private static final Logger log = LoggerFactory.getLogger(GiteaClient.class);

  private final String baseUrl;
  private final String token;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public GiteaClient(
      @Value("${app.gitea.base-url}") String baseUrl, @Value("${app.gitea.token}") String token) {
    this.baseUrl = baseUrl;
    this.token = token;
    this.httpClient = HttpClient.newHttpClient();
  }

  public List<CommitResponse> getCommits(String owner, String repo) {
    if (owner == null || owner.isBlank() || repo == null || repo.isBlank()) {
      return List.of();
    }
    try {
      String url = String.format("%s/api/v1/repos/%s/%s/commits", baseUrl, owner, repo);
      HttpRequest.Builder reqBuilder = HttpRequest.newBuilder().uri(URI.create(url)).GET();
      if (token != null && !token.isBlank()) {
        reqBuilder.header("Authorization", "token " + token);
      }
      HttpRequest req = reqBuilder.build();
      HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
        JsonNode root = objectMapper.readTree(resp.body());
        List<CommitResponse> commits = new ArrayList<>();
        if (root.isArray()) {
          for (JsonNode node : root) {
            String sha = node.has("sha") ? node.get("sha").asText() : null;
            String message = null;
            String authorName = null;
            OffsetDateTime date = null;
            if (node.has("commit")) {
              JsonNode commit = node.get("commit");
              if (commit.has("message")) {
                message = commit.get("message").asText();
              }
              if (commit.has("author")) {
                JsonNode author = commit.get("author");
                if (author.has("name")) {
                  authorName = author.get("name").asText();
                }
                if (author.has("date")) {
                  try {
                    date = OffsetDateTime.parse(author.get("date").asText());
                  } catch (Exception e) {
                    if (log.isDebugEnabled()) {
                      log.debug("Failed to parse commit author date: {}", e.toString(), e);
                    }
                  }
                }
              }
            }
            commits.add(new CommitResponse(sha, message, authorName, date));
          }
        }
        return commits;
      } else {
        if (log.isWarnEnabled()) {
          log.warn("Gitea returned status {} for {}/{}", resp.statusCode(), owner, repo);
        }
        return List.of();
      }
    } catch (IOException | InterruptedException e) {
      if (log.isWarnEnabled()) {
        log.warn("Failed to fetch commits from Gitea", e);
      }
      return List.of();
    }
  }

  public List<WorkflowRunResponse> getWorkflowRuns(String owner, String repo) {
    JsonNode root = getJson(owner, repo, "/actions/runs");
    if (root == null) return List.of();
    JsonNode runs = arrayBody(root, "workflow_runs", "runs");
    List<WorkflowRunResponse> results = new ArrayList<>();
    for (JsonNode run : runs) {
      JsonNode headCommit = run.path("head_commit");
      JsonNode author = headCommit.path("author");
      results.add(
          new WorkflowRunResponse(
              longValue(run, "id", "run_id"),
              text(run, "name", "display_title", "workflow_name"),
              text(run, "status"),
              text(run, "conclusion"),
              text(headCommit, "id", "sha"),
              text(headCommit, "message"),
              text(author, "name", "username", "login"),
              text(run, "head_branch", "branch"),
              date(run, "created_at", "run_started_at"),
              date(run, "updated_at"),
              text(run, "html_url", "url")));
    }
    return results;
  }

  public List<WorkflowJobResponse> getWorkflowJobs(String owner, String repo, Long runId) {
    JsonNode root = getJson(owner, repo, "/actions/runs/" + runId + "/jobs");
    if (root == null) return List.of();
    JsonNode jobs = arrayBody(root, "jobs");
    List<WorkflowJobResponse> results = new ArrayList<>();
    for (JsonNode job : jobs) {
      results.add(
          new WorkflowJobResponse(
              longValue(job, "id", "job_id"),
              text(job, "name"),
              text(job, "status"),
              text(job, "conclusion"),
              date(job, "started_at"),
              date(job, "completed_at"),
              text(job, "html_url", "url")));
    }
    return results;
  }

  public String getWorkflowJobLogs(String owner, String repo, Long runId, Long jobId) {
    if (!hasRepository(owner, repo)) return "";
    String path = "/actions/runs/" + runId + "/jobs/" + jobId + "/logs";
    try {
      HttpResponse<String> response = send(owner, repo, path);
      if (response.statusCode() >= 200 && response.statusCode() < 300) return response.body();
      log.warn("Gitea returned status {} while loading workflow job logs", response.statusCode());
    } catch (IOException e) {
      log.warn("Failed to fetch workflow job logs from Gitea", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Interrupted while fetching workflow job logs from Gitea", e);
    }
    return "";
  }

  private JsonNode getJson(String owner, String repo, String path) {
    if (!hasRepository(owner, repo)) return null;
    try {
      HttpResponse<String> response = send(owner, repo, path);
      if (response.statusCode() >= 200 && response.statusCode() < 300)
        return objectMapper.readTree(response.body());
      log.warn("Gitea returned status {} for workflow request", response.statusCode());
    } catch (IOException e) {
      log.warn("Failed to fetch workflow data from Gitea", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Interrupted while fetching workflow data from Gitea", e);
    }
    return null;
  }

  private HttpResponse<String> send(String owner, String repo, String path)
      throws IOException, InterruptedException {
    String url =
        String.format("%s/api/v1/repos/%s/%s%s", baseUrl, encode(owner), encode(repo), path);
    HttpRequest.Builder request = HttpRequest.newBuilder().uri(URI.create(url)).GET();
    if (token != null && !token.isBlank()) request.header("Authorization", "token " + token);
    return httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
  }

  private boolean hasRepository(String owner, String repo) {
    return owner != null && !owner.isBlank() && repo != null && !repo.isBlank();
  }

  private String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private JsonNode arrayBody(JsonNode root, String... names) {
    if (root.isArray()) return root;
    for (String name : names) if (root.path(name).isArray()) return root.path(name);
    return objectMapper.createArrayNode();
  }

  private String text(JsonNode node, String... names) {
    for (String name : names) if (node.hasNonNull(name)) return node.get(name).asText();
    return null;
  }

  private Long longValue(JsonNode node, String... names) {
    for (String name : names) if (node.hasNonNull(name)) return node.get(name).asLong();
    return null;
  }

  private OffsetDateTime date(JsonNode node, String... names) {
    String value = text(node, names);
    if (value == null || value.isBlank()) return null;
    try {
      return OffsetDateTime.parse(value);
    } catch (Exception exception) {
      log.debug("Unable to parse Gitea workflow date {}", value, exception);
      return null;
    }
  }
}
