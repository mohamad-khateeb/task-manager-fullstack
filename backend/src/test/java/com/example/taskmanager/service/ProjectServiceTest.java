package com.example.taskmanager.service;

import com.example.taskmanager.dto.ProjectDto;
import com.example.taskmanager.entity.Project;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private ProjectDto projectDto;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setDescription("Test Description");

        projectDto = new ProjectDto();
        projectDto.setId(1L);
        projectDto.setName("Test Project");
        projectDto.setDescription("Test Description");
    }

    @Test
    void getAllProjects_ShouldReturnPageOfProjects() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(Arrays.asList(project));

        when(projectRepository.findAll(pageable)).thenReturn(projectPage);

        Page<ProjectDto> result = projectService.getAllProjects(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Project", result.getContent().get(0).getName());
        verify(projectRepository).findAll(pageable);
    }

    @Test
    void getProjectById_WhenExists_ShouldReturnProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectDto result = projectService.getProjectById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Project", result.getName());
        verify(projectRepository).findById(1L);
    }

    @Test
    void getProjectById_WhenNotExists_ShouldThrowException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectById(1L));
        verify(projectRepository).findById(1L);
    }

    @Test
    void createProject_ShouldSaveAndReturnProject() {
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectDto result = projectService.createProject(projectDto);

        assertNotNull(result);
        assertEquals("Test Project", result.getName());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void updateProject_WhenExists_ShouldUpdateAndReturnProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectDto result = projectService.updateProject(1L, projectDto);

        assertNotNull(result);
        verify(projectRepository).findById(1L);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void updateProject_WhenNotExists_ShouldThrowException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.updateProject(1L, projectDto));
        verify(projectRepository).findById(1L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void deleteProject_WhenExists_ShouldDeleteProject() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        doNothing().when(projectRepository).deleteById(1L);

        projectService.deleteProject(1L);

        verify(projectRepository).existsById(1L);
        verify(projectRepository).deleteById(1L);
    }

    @Test
    void deleteProject_WhenNotExists_ShouldThrowException() {
        when(projectRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> projectService.deleteProject(1L));
        verify(projectRepository).existsById(1L);
        verify(projectRepository, never()).deleteById(anyLong());
    }
}

