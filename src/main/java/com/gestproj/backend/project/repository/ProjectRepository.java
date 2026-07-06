package com.gestproj.backend.project.repository;

import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByOwner(User owner);
}
