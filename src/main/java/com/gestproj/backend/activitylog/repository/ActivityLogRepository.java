package com.gestproj.backend.activitylog.repository;

import com.gestproj.backend.activitylog.entity.ActivityLog;
import com.gestproj.backend.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findAllByProjectOrderByCreatedAtDesc(Project project);
}
