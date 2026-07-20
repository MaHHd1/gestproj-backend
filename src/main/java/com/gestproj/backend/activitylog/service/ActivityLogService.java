package com.gestproj.backend.activitylog.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gestproj.backend.activitylog.dto.ActivityLogResponse;
import com.gestproj.backend.activitylog.entity.ActivityLog;
import com.gestproj.backend.activitylog.repository.ActivityLogRepository;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.user.entity.User;

@Service
public class ActivityLogService {

  private final ActivityLogRepository activityLogRepository;

  public ActivityLogService(ActivityLogRepository activityLogRepository) {
    this.activityLogRepository = activityLogRepository;
  }

  public ActivityLog log(Project project, User user, String action) {
    ActivityLog activityLog = new ActivityLog();
    activityLog.setProject(project);
    activityLog.setUser(user);
    activityLog.setAction(action);
    activityLog.setCreatedAt(LocalDateTime.now());
    return activityLogRepository.save(activityLog);
  }

  public List<ActivityLogResponse> listForProject(Project project) {
    return activityLogRepository.findAllByProjectOrderByCreatedAtDesc(project).stream()
        .map(this::toResponse)
        .toList();
  }

  private ActivityLogResponse toResponse(ActivityLog activityLog) {
    return new ActivityLogResponse(
        activityLog.getId(),
        activityLog.getProject().getId(),
        activityLog.getUser().getId(),
        activityLog.getUser().getUsername(),
        activityLog.getAction(),
        activityLog.getCreatedAt());
  }
}
