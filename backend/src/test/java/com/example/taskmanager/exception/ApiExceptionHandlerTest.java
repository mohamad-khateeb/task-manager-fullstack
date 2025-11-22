package com.example.taskmanager.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiExceptionHandlerTest {

    @InjectMocks
    private ApiExceptionHandler exceptionHandler;

    @Test
    void handleResourceNotFoundException_ShouldReturnNotFound() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Project not found with id: 1");

        ResponseEntity<ApiExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleResourceNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals("Project not found with id: 1", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequest() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<org.springframework.validation.ObjectError> errors = new ArrayList<>();
        
        FieldError fieldError = new FieldError("projectDto", "name", "Project name is required");
        errors.add(fieldError);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(errors);

        ResponseEntity<ApiExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Validation failed"));
        assertTrue(response.getBody().getMessage().contains("name"));
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleNoResourceFoundException_ShouldReturnNotFound() {
        NoResourceFoundException exception = new NoResourceFoundException(
                org.springframework.http.HttpMethod.GET, "/favicon.ico");

        ResponseEntity<Void> response = exceptionHandler.handleNoResourceFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        Exception exception = new Exception("Unexpected error occurred");

        ResponseEntity<ApiExceptionHandler.ErrorResponse> response = 
                exceptionHandler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Unexpected error occurred"));
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void errorResponse_ShouldHaveCorrectFields() {
        LocalDateTime timestamp = LocalDateTime.now();
        ApiExceptionHandler.ErrorResponse errorResponse = 
                new ApiExceptionHandler.ErrorResponse(404, "Not found", timestamp);

        assertEquals(404, errorResponse.getStatus());
        assertEquals("Not found", errorResponse.getMessage());
        assertEquals(timestamp, errorResponse.getTimestamp());

        errorResponse.setStatus(500);
        errorResponse.setMessage("Server error");
        errorResponse.setTimestamp(LocalDateTime.now().plusHours(1));

        assertEquals(500, errorResponse.getStatus());
        assertEquals("Server error", errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
    }
}

