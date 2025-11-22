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
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        
        logger.info("Authenticating user: {} (attempting with email)", email);

        try (CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            // Try with email first
            InitiateAuthResponse authResponse = attemptAuthentication(cognitoClient, email, password);
            
            // Check if authentication requires new password (temporary password)
            if (authResponse.challengeName() == ChallengeNameType.NEW_PASSWORD_REQUIRED) {
                logger.warn("User {} requires new password (temporary password detected)", email);
                throw new RuntimeException("Temporary password detected. Please change your password first. " +
                        "You can do this in AWS Cognito Console or contact administrator.");
            }

            AuthenticationResultType authResult = authResponse.authenticationResult();
            
            if (authResult == null) {
                logger.error("Authentication result is null for user: {}", email);
                throw new RuntimeException("Authentication failed: No authentication result received");
            }

            logger.info("Authentication successful for user: {}", email);

            return new LoginResponse(
                    authResult.idToken(),
                    authResult.accessToken(),
                    authResult.refreshToken(),
                    authResult.expiresIn() != null ? authResult.expiresIn().longValue() : null
            );

        } catch (NotAuthorizedException e) {
            logger.error("NotAuthorizedException for user: {} - Error: {}, Error Code: {}", 
                    email, e.getMessage(), e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "N/A");
            throw new RuntimeException("Invalid email or password. Please check your credentials.", e);
        } catch (UserNotConfirmedException e) {
            logger.error("UserNotConfirmedException for user: {} - Error: {}", email, e.getMessage());
            throw new RuntimeException("User account is not confirmed. Please verify your email address in AWS Cognito.", e);
        } catch (UserNotFoundException e) {
            logger.error("UserNotFoundException for user: {} - Error: {}", email, e.getMessage());
            throw new RuntimeException("User not found. Please check your email address or contact administrator.", e);
        } catch (PasswordResetRequiredException e) {
            logger.error("PasswordResetRequiredException for user: {} - Error: {}", email, e.getMessage());
            throw new RuntimeException("Password reset required. Please reset your password in AWS Cognito.", e);
        } catch (CognitoIdentityProviderException e) {
            String errorCode = e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "UNKNOWN";
            String errorMessage = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
            logger.error("CognitoIdentityProviderException for user: {} - Error Code: {}, Error: {}, Full Exception: ", 
                    email, errorCode, errorMessage, e);
            throw new RuntimeException("Authentication failed: " + errorMessage + " (Error Code: " + errorCode + ")", e);
        } catch (RuntimeException e) {
            // Re-throw our custom runtime exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during authentication for user: {} - Error: {}, Class: {}", 
                    email, e.getMessage(), e.getClass().getName(), e);
            throw new RuntimeException("An unexpected error occurred during authentication: " + e.getMessage(), e);
        }
    }

    private InitiateAuthResponse attemptAuthentication(CognitoIdentityProviderClient cognitoClient, 
                                                       String usernameOrEmail, String password) {
        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", usernameOrEmail);
        authParams.put("PASSWORD", password);

        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .clientId(appClientId)
                .authParameters(authParams)
                .build();

        logger.debug("Attempting authentication with USERNAME: {}", usernameOrEmail);
        return cognitoClient.initiateAuth(authRequest);
    }
}

