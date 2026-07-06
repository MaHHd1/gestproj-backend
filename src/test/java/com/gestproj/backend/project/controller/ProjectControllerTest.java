package com.gestproj.backend.project.controller;

import com.gestproj.backend.project.dto.ProjectCreateRequest;
import com.gestproj.backend.project.dto.ProjectResponse;
import com.gestproj.backend.project.dto.ProjectUpdateRequest;
import com.gestproj.backend.project.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectController projectController;

    @Test
    void createShouldReturnCreatedProject() {
        when(authentication.getName()).thenReturn("owner@example.com");
        when(projectService.create(any(ProjectCreateRequest.class), eq("owner@example.com")))
                .thenReturn(new ProjectResponse(1L, "Alpha", "Main project", 2L, "owner"));

        var response = projectController.create(new ProjectCreateRequest("Alpha", "Main project"), authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Alpha", response.getBody().name());
    }

    @Test
    void listShouldReturnProjects() {
        when(authentication.getName()).thenReturn("owner@example.com");
        when(projectService.getProjectsForCurrentUser("owner@example.com"))
                .thenReturn(List.of(new ProjectResponse(1L, "Alpha", "Main project", 2L, "owner")));

        var response = projectController.list(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void updateShouldReturnUpdatedProject() {
        when(authentication.getName()).thenReturn("owner@example.com");
        when(projectService.update(eq(1L), any(ProjectUpdateRequest.class), eq("owner@example.com")))
                .thenReturn(new ProjectResponse(1L, "Beta", "Updated", 2L, "owner"));

        var response = projectController.update(1L, new ProjectUpdateRequest("Beta", "Updated"), authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Beta", response.getBody().name());
    }

    @Test
    void deleteShouldReturnNoContent() {
        when(authentication.getName()).thenReturn("owner@example.com");

        var response = projectController.delete(1L, authentication);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
