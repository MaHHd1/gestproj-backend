package com.gestproj.backend.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestproj.backend.common.enums.ProjectMemberStatus;
import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.user.entity.User;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
  boolean existsByProjectAndUser(Project project, User user);

  boolean existsByProjectAndUserAndStatus(Project project, User user, ProjectMemberStatus status);

  Optional<ProjectMember> findByProjectAndUser(Project project, User user);

  Optional<ProjectMember> findByProjectAndUserAndStatus(
      Project project, User user, ProjectMemberStatus status);

  List<ProjectMember> findAllByUser(User user);

  List<ProjectMember> findAllByProject(Project project);

  void deleteAllByProject(Project project);
}
