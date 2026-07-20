package com.gestproj.backend.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.user.entity.User;

public interface ProjectRepository extends JpaRepository<Project, Long> {
  List<Project> findAllByOwner(User owner);
}
