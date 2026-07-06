package com.gestproj.backend.member.repository;

import com.gestproj.backend.member.entity.ProjectMember;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    boolean existsByProjectAndUser(Project project, User user);
    Optional<ProjectMember> findByProjectAndUser(Project project, User user);
    List<ProjectMember> findAllByUser(User user);
    List<ProjectMember> findAllByProject(Project project);
}
