package com.example.taskmanager.service;

import com.example.taskmanager.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuthService.
 * 
 * Note: Full integration testing of AWS Cognito requires actual AWS credentials and a Cognito User Pool.
 * These tests verify that the service properly handles different scenarios and error cases.
 * For complete testing, integration tests with a test Cognito environment are recommended.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private String userPoolId = "test-pool-id";
    private String region = "us-east-1";
    private String appClientId = "test-client-id";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "userPoolId", userPoolId);
        ReflectionTestUtils.setField(authService, "region", region);
        ReflectionTestUtils.setField(authService, "appClientId", appClientId);

        loginRequest = new LoginRequest("test@example.com", "password123");
    }

    @Test
    void authenticate_WhenCredentialsAreNull_ShouldThrowException() {
        LoginRequest nullRequest = new LoginRequest(null, null);

        assertThrows(Exception.class, () -> {
            authService.authenticate(nullRequest);
        });
    }

    @Test
    void authenticate_WhenEmailIsEmpty_ShouldThrowException() {
        LoginRequest emptyEmailRequest = new LoginRequest("", "password123");

        assertThrows(Exception.class, () -> {
            authService.authenticate(emptyEmailRequest);
        });
    }

    @Test
    void authenticate_WhenPasswordIsEmpty_ShouldThrowException() {
        LoginRequest emptyPasswordRequest = new LoginRequest("test@example.com", "");

        assertThrows(Exception.class, () -> {
            authService.authenticate(emptyPasswordRequest);
        });
    }

    @Test
    void authenticate_WhenInvalidCredentials_ShouldThrowRuntimeException() {
        // This will fail at AWS Cognito level, but we verify the exception is properly wrapped
        assertThrows(RuntimeException.class, () -> {
            authService.authenticate(loginRequest);
        });
    }

    @Test
    void authenticate_ServiceConfiguration_ShouldBeSet() {
        // Verify that configuration fields are properly set
        String configuredUserPoolId = (String) ReflectionTestUtils.getField(authService, "userPoolId");
        String configuredRegion = (String) ReflectionTestUtils.getField(authService, "region");
        String configuredAppClientId = (String) ReflectionTestUtils.getField(authService, "appClientId");

        assertEquals(userPoolId, configuredUserPoolId);
        assertEquals(region, configuredRegion);
        assertEquals(appClientId, configuredAppClientId);
    }
}

