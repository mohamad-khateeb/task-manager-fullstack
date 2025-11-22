package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.entity.Project;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.exception.ResourceNotFoundException;
import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.TaskRepository;
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
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private TaskService taskService;

    private Project project;
    private Task task;
    private TaskDto taskDto;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(Task.TaskStatus.TODO);
        task.setProject(project);

        taskDto = new TaskDto();
        taskDto.setId(1L);
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(Task.TaskStatus.TODO);
        taskDto.setProjectId(1L);
    }

    @Test
    void getTasksByProjectId_WhenProjectExists_ShouldReturnPageOfTasks() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(Arrays.asList(task));

        when(projectRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findByProjectId(1L, pageable)).thenReturn(taskPage);

        Page<TaskDto> result = taskService.getTasksByProjectId(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Task", result.getContent().get(0).getTitle());
        verify(projectRepository).existsById(1L);
        verify(taskRepository).findByProjectId(1L, pageable);
    }

    @Test
    void getTasksByProjectId_WhenProjectNotExists_ShouldThrowException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(projectRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTasksByProjectId(1L, pageable));
        verify(projectRepository).existsById(1L);
        verify(taskRepository, never()).findByProjectId(anyLong(), any(Pageable.class));
    }

    @Test
    void getTaskById_WhenExists_ShouldReturnTask() {
        when(taskRepository.findByIdAndProjectId(1L, 1L)).thenReturn(Optional.of(task));

        TaskDto result = taskService.getTaskById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository).findByIdAndProjectId(1L, 1L);
    }

    @Test
    void getTaskById_WhenNotExists_ShouldThrowException() {
        when(taskRepository.findByIdAndProjectId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(1L, 1L));
        verify(taskRepository).findByIdAndProjectId(1L, 1L);
    }

    @Test
    void createTask_WhenProjectExists_ShouldSaveAndReturnTask() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskDto result = taskService.createTask(1L, taskDto);

        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(projectRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_WhenProjectNotExists_ShouldThrowException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(1L, taskDto));
        verify(projectRepository).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_WhenExists_ShouldUpdateAndReturnTask() {
        when(taskRepository.findByIdAndProjectId(1L, 1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskDto result = taskService.updateTask(1L, 1L, taskDto);

        assertNotNull(result);
        verify(taskRepository).findByIdAndProjectId(1L, 1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_WhenNotExists_ShouldThrowException() {
        when(taskRepository.findByIdAndProjectId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(1L, 1L, taskDto));
        verify(taskRepository).findByIdAndProjectId(1L, 1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTask_WhenExists_ShouldDeleteTask() {
        when(taskRepository.findByIdAndProjectId(1L, 1L)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).delete(task);

        taskService.deleteTask(1L, 1L);

        verify(taskRepository).findByIdAndProjectId(1L, 1L);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_WhenNotExists_ShouldThrowException() {
        when(taskRepository.findByIdAndProjectId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(1L, 1L));
        verify(taskRepository).findByIdAndProjectId(1L, 1L);
        verify(taskRepository, never()).delete(any(Task.class));
    }
}

