package com.gestproj.backend.deployment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestproj.backend.deployment.entity.Deployment;

public interface DeploymentRepository extends JpaRepository<Deployment, Long> {
  List<Deployment> findAllByProjectIdOrderByFinishedAtDesc(Long projectId);
}
