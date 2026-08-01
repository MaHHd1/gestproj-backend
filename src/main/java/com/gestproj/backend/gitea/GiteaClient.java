package com.gestproj.backend.gitea;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

@Service
public class GiteaClient {

  private static final Logger log = LoggerFactory.getLogger(GiteaClient.class);

  private final String baseUrl;
  private final String token;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public GiteaClient(@Value("${app.gitea.base-url}") String baseUrl, @Value("${app.gitea.token}") String token) {
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
}
