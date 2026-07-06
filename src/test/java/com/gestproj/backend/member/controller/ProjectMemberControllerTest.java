package com.gestproj.backend.member.controller;

import com.gestproj.backend.common.enums.ProjectMemberRole;
import com.gestproj.backend.common.enums.ProjectMemberStatus;
import com.gestproj.backend.member.dto.ProjectMemberResponse;
import com.gestproj.backend.member.service.ProjectMemberService;
import com.gestproj.backend.project.entity.Project;
import com.gestproj.backend.project.repository.ProjectRepository;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectMemberControllerTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberService projectMemberService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectMemberController controller;

    @Test
    void listShouldReturnMembers() {
        Project project = new Project();
        project.setName("Alpha");

        User user = new User();
        user.setEmail("owner@example.com");

        when(authentication.getName()).thenReturn("owner@example.com");
        when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
        when(userService.findEntityByEmail("owner@example.com")).thenReturn(user);
        when(projectMemberService.findProjectMember(project, user)).thenReturn(
                new com.gestproj.backend.member.entity.ProjectMember()
        );
        when(projectMemberService.getProjectMembers(project)).thenReturn(
                List.of(new ProjectMemberResponse(1L, 1L, 2L, "member", ProjectMemberRole.MEMBER, ProjectMemberStatus.ACTIVE, "Tester", "QA", true, true, false, false, false, false))
        );

        var response = controller.list(1L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("member", response.getBody().get(0).username());
    }
}
