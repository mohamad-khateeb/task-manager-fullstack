package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.service.TaskService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private TaskDto taskDto;

    @BeforeEach
    void setUp() {
        taskDto = new TaskDto();
        taskDto.setId(1L);
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");
        taskDto.setStatus(Task.TaskStatus.TODO);
        taskDto.setProjectId(1L);
    }

    @Test
    void getTasksByProjectId_ShouldReturnPageOfTasks() {
        Page<TaskDto> taskPage = new PageImpl<>(Arrays.asList(taskDto));
        when(taskService.getTasksByProjectId(eq(1L), any())).thenReturn(taskPage);

        ResponseEntity<Page<TaskDto>> response = taskController.getTasksByProjectId(1L, PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(taskService).getTasksByProjectId(eq(1L), any());
    }

    @Test
    void getTaskById_ShouldReturnTask() {
        when(taskService.getTaskById(1L, 1L)).thenReturn(taskDto);

        ResponseEntity<TaskDto> response = taskController.getTaskById(1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test Task", response.getBody().getTitle());
        verify(taskService).getTaskById(1L, 1L);
    }

    @Test
    void createTask_ShouldReturnCreatedTask() {
        when(taskService.createTask(eq(1L), any(TaskDto.class))).thenReturn(taskDto);

        ResponseEntity<TaskDto> response = taskController.createTask(1L, taskDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Task", response.getBody().getTitle());
        verify(taskService).createTask(eq(1L), any(TaskDto.class));
    }

    @Test
    void updateTask_ShouldReturnUpdatedTask() {
        when(taskService.updateTask(eq(1L), eq(1L), any(TaskDto.class))).thenReturn(taskDto);

        ResponseEntity<TaskDto> response = taskController.updateTask(1L, 1L, taskDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(taskService).updateTask(eq(1L), eq(1L), any(TaskDto.class));
    }

    @Test
    void deleteTask_ShouldReturnNoContent() {
        doNothing().when(taskService).deleteTask(1L, 1L);

        ResponseEntity<Void> response = taskController.deleteTask(1L, 1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(taskService).deleteTask(1L, 1L);
    }
}

