package com.example.taskmanager.service;

import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${cognito.userPoolId}")
    private String userPoolId;

    @Value("${cognito.region}")
    private String region;

    @Value("${cognito.appClientId}")
    private String appClientId;

    public LoginResponse authenticate(LoginRequest loginRequest) {
        logger.info("Authenticating user: {}", loginRequest.getEmail());

        try (CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", loginRequest.getEmail());
            authParams.put("PASSWORD", loginRequest.getPassword());

            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(appClientId)
                    .authParameters(authParams)
                    .build();

            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);

            AuthenticationResultType authResult = authResponse.authenticationResult();

            logger.info("Authentication successful for user: {}", loginRequest.getEmail());

            return new LoginResponse(
                    authResult.idToken(),
                    authResult.accessToken(),
                    authResult.refreshToken(),
                    authResult.expiresIn() != null ? authResult.expiresIn().longValue() : null
            );

        } catch (NotAuthorizedException e) {
            logger.warn("Authentication failed for user: {} - {}", loginRequest.getEmail(), e.getMessage());
            throw new RuntimeException("Invalid email or password", e);
        } catch (UserNotConfirmedException e) {
            logger.warn("User not confirmed: {}", loginRequest.getEmail());
            throw new RuntimeException("User account is not confirmed. Please verify your email.", e);
        } catch (UserNotFoundException e) {
            logger.warn("User not found: {}", loginRequest.getEmail());
            throw new RuntimeException("User not found", e);
        } catch (CognitoIdentityProviderException e) {
            logger.error("Cognito error during authentication: {}", e.getMessage(), e);
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during authentication: {}", e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred", e);
        }
    }
}

