package com.gestproj.backend.task.repository;

import com.gestproj.backend.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
