package com.example.taskmanager.service;

import com.example.taskmanager.dto.ProjectDto;
import com.example.taskmanager.entity.Project;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Page<ProjectDto> getAllProjects(Pageable pageable) {
        logger.info("Fetching all projects with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return projectRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public ProjectDto getProjectById(Long id) {
        logger.info("Fetching project with id: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Project not found with id: {}", id);
                    return new ResourceNotFoundException("Project not found with id: " + id);
                });
        return convertToDto(project);
    }

    public ProjectDto createProject(ProjectDto projectDto) {
        logger.info("Creating new project: {}", projectDto.getName());
        Project project = convertToEntity(projectDto);
        Project savedProject = projectRepository.save(project);
        logger.info("Project created successfully with id: {}", savedProject.getId());
        return convertToDto(savedProject);
    }

    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        logger.info("Updating project with id: {}", id);
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Project not found with id: {}", id);
                    return new ResourceNotFoundException("Project not found with id: " + id);
                });
        
        project.setName(projectDto.getName());
        project.setDescription(projectDto.getDescription());
        Project updatedProject = projectRepository.save(project);
        logger.info("Project updated successfully with id: {}", updatedProject.getId());
        return convertToDto(updatedProject);
    }

    public void deleteProject(Long id) {
        logger.info("Deleting project with id: {}", id);
        if (!projectRepository.existsById(id)) {
            logger.warn("Project not found with id: {}", id);
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
        logger.info("Project deleted successfully with id: {}", id);
    }

    private ProjectDto convertToDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        return dto;
    }

    private Project convertToEntity(ProjectDto dto) {
        Project project = new Project();
        project.setId(dto.getId());
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        return project;
    }
}

