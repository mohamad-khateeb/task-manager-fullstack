package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ProjectDto;
import com.example.taskmanager.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    private ProjectDto projectDto;

    @BeforeEach
    void setUp() {
        projectDto = new ProjectDto();
        projectDto.setId(1L);
        projectDto.setName("Test Project");
        projectDto.setDescription("Test Description");
    }

    @Test
    void getAllProjects_ShouldReturnPageOfProjects() {
        Page<ProjectDto> projectPage = new PageImpl<>(Arrays.asList(projectDto));
        when(projectService.getAllProjects(any())).thenReturn(projectPage);

        ResponseEntity<Page<ProjectDto>> response = projectController.getAllProjects(PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(projectService).getAllProjects(any());
    }

    @Test
    void getProjectById_ShouldReturnProject() {
        when(projectService.getProjectById(1L)).thenReturn(projectDto);

        ResponseEntity<ProjectDto> response = projectController.getProjectById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(projectService).getProjectById(1L);
    }

    @Test
    void createProject_ShouldReturnCreatedProject() {
        when(projectService.createProject(any(ProjectDto.class))).thenReturn(projectDto);

        ResponseEntity<ProjectDto> response = projectController.createProject(projectDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(projectService).createProject(any(ProjectDto.class));
    }

    @Test
    void updateProject_ShouldReturnUpdatedProject() {
        when(projectService.updateProject(eq(1L), any(ProjectDto.class))).thenReturn(projectDto);

        ResponseEntity<ProjectDto> response = projectController.updateProject(1L, projectDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(projectService).updateProject(eq(1L), any(ProjectDto.class));
    }

    @Test
    void deleteProject_ShouldReturnNoContent() {
        doNothing().when(projectService).deleteProject(1L);

        ResponseEntity<Void> response = projectController.deleteProject(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(projectService).deleteProject(1L);
    }
}

