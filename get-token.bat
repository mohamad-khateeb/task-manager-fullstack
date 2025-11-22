@echo off
REM AWS Cognito Token Retrieval Script for Windows
REM This script helps you get an ID token from AWS Cognito

REM ============================================
REM CONFIGURATION - UPDATE THESE VALUES
REM ============================================
set CLIENT_ID=YOUR_APP_CLIENT_ID_HERE
set USERNAME=admin
set PASSWORD=YourPassword123!
set REGION=eu-north-1

REM ============================================
REM SCRIPT - DON'T MODIFY BELOW
REM ============================================

echo 🔐 Getting ID Token from AWS Cognito...
echo.

REM Check if AWS CLI is installed
where aws >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ AWS CLI is not installed!
    echo    Install it: https://aws.amazon.com/cli/
    exit /b 1
)

REM Check if configured
aws sts get-caller-identity >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ AWS CLI is not configured!
    echo    Run: aws configure
    exit /b 1
)

REM Validate configuration
if "%CLIENT_ID%"=="YOUR_APP_CLIENT_ID_HERE" (
    echo ❌ Please update CLIENT_ID in this script!
    echo    Edit get-token.bat and set your App Client ID
    exit /b 1
)

REM Get the token
echo 📡 Authenticating user: %USERNAME%
echo.

aws cognito-idp initiate-auth --region %REGION% --auth-flow USER_PASSWORD_AUTH --client-id %CLIENT_ID% --auth-parameters USERNAME=%USERNAME%,PASSWORD=%PASSWORD% > temp_response.json 2>&1

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Authentication failed!
    echo.
    echo Error details:
    type temp_response.json
    echo.
    echo 💡 Common issues:
    echo    - Wrong username or password
    echo    - User email not verified
    echo    - Wrong CLIENT_ID
    echo    - Wrong REGION
    del temp_response.json 2>nul
    exit /b 1
)

REM Extract ID token (requires jq or manual extraction)
echo ✅ Success! Check temp_response.json for your ID token
echo.
echo 💡 Look for the "IdToken" field in the JSON response
echo.
echo 📋 Copy the token value and paste it in your login form at:
echo    http://localhost:3000/login
echo.

REM Note: For Windows, you might want to install jq for better parsing
REM Or manually open temp_response.json and copy the IdToken value

