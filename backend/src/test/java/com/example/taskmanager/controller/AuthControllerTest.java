package com.example.taskmanager.controller;

import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.LoginResponse;
import com.example.taskmanager.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("test@example.com", "password123");
        
        loginResponse = new LoginResponse(
            "id-token-123",
            "access-token-123",
            "refresh-token-123",
            3600L
        );
    }

    @Test
    void login_WhenValidCredentials_ShouldReturnLoginResponse() {
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(loginResponse);

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof LoginResponse);
        LoginResponse responseBody = (LoginResponse) response.getBody();
        assertEquals("id-token-123", responseBody.getIdToken());
        verify(authService).authenticate(any(LoginRequest.class));
    }

    @Test
    void login_WhenInvalidCredentials_ShouldReturnUnauthorized() {
        RuntimeException exception = new RuntimeException("Invalid email or password. Please check your credentials.");
        when(authService.authenticate(any(LoginRequest.class))).thenThrow(exception);

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> error = (Map<String, String>) response.getBody();
        assertTrue(error.containsKey("message"));
        assertTrue(error.get("message").contains("Invalid email or password"));
        verify(authService).authenticate(any(LoginRequest.class));
    }

    @Test
    void login_WhenUserNotConfirmed_ShouldReturnForbidden() {
        RuntimeException exception = new RuntimeException("User account is not confirmed. Please verify your email address in AWS Cognito.");
        when(authService.authenticate(any(LoginRequest.class))).thenThrow(exception);

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authService).authenticate(any(LoginRequest.class));
    }

    @Test
    void login_WhenUserNotFound_ShouldReturnNotFound() {
        RuntimeException exception = new RuntimeException("User not found. Please check your email address or contact administrator.");
        when(authService.authenticate(any(LoginRequest.class))).thenThrow(exception);

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authService).authenticate(any(LoginRequest.class));
    }

    @Test
    void login_WhenTemporaryPassword_ShouldReturnForbidden() {
        RuntimeException exception = new RuntimeException("Temporary password detected. Please change your password first.");
        when(authService.authenticate(any(LoginRequest.class))).thenThrow(exception);

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authService).authenticate(any(LoginRequest.class));
    }

    @Test
    void diagnostic_ShouldReturnOk() {
        ResponseEntity<Map<String, String>> response = authController.diagnostic();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, String> body = response.getBody();
        assertEquals("ok", body.get("status"));
        assertEquals("Authentication endpoint is available", body.get("message"));
        assertEquals("/api/auth/login", body.get("endpoint"));
        assertEquals("POST", body.get("method"));
    }
}

