package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.entity.Project;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public Page<TaskDto> getTasksByProjectId(Long projectId, Pageable pageable) {
        logger.info("Fetching tasks for project id: {} with pagination: page={}, size={}", 
                projectId, pageable.getPageNumber(), pageable.getPageSize());
        if (!projectRepository.existsById(projectId)) {
            logger.warn("Project not found with id: {}", projectId);
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        return taskRepository.findByProjectId(projectId, pageable)
                .map(this::convertToDto);
    }

    public TaskDto getTaskById(Long projectId, Long taskId) {
        logger.info("Fetching task with id: {} for project id: {}", taskId, projectId);
        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> {
                    logger.warn("Task not found with id: {} for project id: {}", taskId, projectId);
                    return new ResourceNotFoundException("Task not found with id: " + taskId + " for project id: " + projectId);
                });
        return convertToDto(task);
    }

    public TaskDto createTask(Long projectId, TaskDto taskDto) {
        logger.info("Creating new task for project id: {}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> {
                    logger.warn("Project not found with id: {}", projectId);
                    return new ResourceNotFoundException("Project not found with id: " + projectId);
                });
        
        Task task = convertToEntity(taskDto);
        task.setProject(project);
        if (task.getStatus() == null) {
            task.setStatus(Task.TaskStatus.TODO);
        }
        Task savedTask = taskRepository.save(task);
        logger.info("Task created successfully with id: {}", savedTask.getId());
        return convertToDto(savedTask);
    }

    public TaskDto updateTask(Long projectId, Long taskId, TaskDto taskDto) {
        logger.info("Updating task with id: {} for project id: {}", taskId, projectId);
        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> {
                    logger.warn("Task not found with id: {} for project id: {}", taskId, projectId);
                    return new ResourceNotFoundException("Task not found with id: " + taskId + " for project id: " + projectId);
                });
        
        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        if (taskDto.getStatus() != null) {
            task.setStatus(taskDto.getStatus());
        }
        Task updatedTask = taskRepository.save(task);
        logger.info("Task updated successfully with id: {}", updatedTask.getId());
        return convertToDto(updatedTask);
    }

    public void deleteTask(Long projectId, Long taskId) {
        logger.info("Deleting task with id: {} for project id: {}", taskId, projectId);
        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> {
                    logger.warn("Task not found with id: {} for project id: {}", taskId, projectId);
                    return new ResourceNotFoundException("Task not found with id: " + taskId + " for project id: " + projectId);
                });
        taskRepository.delete(task);
        logger.info("Task deleted successfully with id: {}", taskId);
    }

    private TaskDto convertToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setProjectId(task.getProject().getId());
        return dto;
    }

    private Task convertToEntity(TaskDto dto) {
        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : Task.TaskStatus.TODO);
        return task;
    }
}

