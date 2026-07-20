package com.gestproj.backend.task.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.task.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
  void deleteAllByProject(Project project);
}
