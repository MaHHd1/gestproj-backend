package com.gestproj.backend.task.repository;

import com.gestproj.backend.task.entity.Task;
import com.gestproj.backend.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    void deleteAllByProject(Project project);
}
