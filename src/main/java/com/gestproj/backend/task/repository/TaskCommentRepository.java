package com.gestproj.backend.task.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestproj.backend.task.entity.TaskComment;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
  List<TaskComment> findByTaskIdOrderByCreatedAtDesc(Long taskId);
}
