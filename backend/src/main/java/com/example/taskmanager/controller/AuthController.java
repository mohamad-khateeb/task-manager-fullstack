package com.example.taskmanager.controller;

import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.LoginResponse;
import com.example.taskmanager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.authenticate(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Return error response with detailed message
            Map<String, String> error = new HashMap<>();
            String message = e.getMessage();
            error.put("message", message);
            
            // Add error code if available
            if (message.contains("Error Code:")) {
                error.put("errorCode", extractErrorCode(message));
            }
            
            // Determine appropriate HTTP status
            HttpStatus status = determineHttpStatus(message);
            return ResponseEntity.status(status).body(error);
        }
    }

    private String extractErrorCode(String message) {
        if (message.contains("Error Code:")) {
            int start = message.indexOf("Error Code:") + 11;
            int end = message.indexOf(")", start);
            if (end == -1) end = message.length();
            return message.substring(start, end).trim();
        }
        return null;
    }

    private HttpStatus determineHttpStatus(String message) {
        if (message.contains("not confirmed") || message.contains("verify your email")) {
            return HttpStatus.FORBIDDEN; // 403
        } else if (message.contains("not found")) {
            return HttpStatus.NOT_FOUND; // 404
        } else if (message.contains("Temporary password") || message.contains("reset required")) {
            return HttpStatus.FORBIDDEN; // 403
        }
        return HttpStatus.UNAUTHORIZED; // 401 (default)
    }

    @GetMapping("/diagnostic")
    public ResponseEntity<Map<String, String>> diagnostic() {
        Map<String, String> info = new HashMap<>();
        info.put("status", "ok");
        info.put("message", "Authentication endpoint is available");
        info.put("endpoint", "/api/auth/login");
        info.put("method", "POST");
        return ResponseEntity.ok(info);
    }
}

