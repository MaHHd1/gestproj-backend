package com.gestproj.backend.activitylog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestproj.backend.activitylog.entity.ActivityLog;
import com.gestproj.backend.project.entity.Project;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
  List<ActivityLog> findAllByProjectOrderByCreatedAtDesc(Project project);

  void deleteAllByProject(Project project);
}
