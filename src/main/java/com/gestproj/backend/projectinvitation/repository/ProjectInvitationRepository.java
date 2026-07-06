package com.gestproj.backend.projectinvitation.repository;

import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.projectinvitation.entity.ProjectInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long> {
    Optional<ProjectInvitation> findByToken(String token);
    List<ProjectInvitation> findAllByProjectOrderByCreatedAtDesc(Project project);
}
