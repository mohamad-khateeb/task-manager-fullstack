#!/bin/bash

# Test User Login Script
# Tests login for a specific user and shows detailed error information

# Configuration
CLIENT_ID="5jimq4cdhoov9p9a8e4btcn99h"
REGION="eu-north-1"
USERNAME="moveo@gmail.com"
PASSWORD="User123!"
BACKEND_URL="http://localhost:8080"

echo "🔍 Testing Login for User: $USERNAME"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Test 1: Check Backend Diagnostic Endpoint
echo "Test 1: Checking backend diagnostic endpoint..."
DIAGNOSTIC=$(curl -s "$BACKEND_URL/api/auth/diagnostic" 2>&1)
if echo "$DIAGNOSTIC" | grep -q "ok"; then
    echo "✅ Backend is running and accessible"
else
    echo "❌ Backend is not accessible"
    echo "   Response: $DIAGNOSTIC"
    echo "   Make sure backend is running: cd backend && mvn spring-boot:run"
    exit 1
fi
echo ""

# Test 2: Test Direct Cognito Authentication
echo "Test 2: Testing direct Cognito authentication..."
echo "   This will show the exact error from AWS Cognito"
echo ""

COGNITO_RESPONSE=$(aws cognito-idp initiate-auth \
    --region $REGION \
    --auth-flow USER_PASSWORD_AUTH \
    --client-id $CLIENT_ID \
    --auth-parameters USERNAME=$USERNAME,PASSWORD=$PASSWORD \
    2>&1)

if [ $? -eq 0 ]; then
    echo "✅ Direct Cognito authentication SUCCESSFUL"
    echo "   User credentials are correct"
    ID_TOKEN=$(echo "$COGNITO_RESPONSE" | grep -o '"IdToken":"[^"]*' | cut -d'"' -f4)
    if [ -n "$ID_TOKEN" ]; then
        echo "   ID Token received: ${ID_TOKEN:0:50}..."
    fi
else
    echo "❌ Direct Cognito authentication FAILED"
    echo ""
    echo "   Error Details:"
    echo "$COGNITO_RESPONSE" | grep -E "(__type|message|errorCode)" || echo "$COGNITO_RESPONSE"
    echo ""
    echo "   Common Issues:"
    if echo "$COGNITO_RESPONSE" | grep -q "UserNotConfirmedException"; then
        echo "   → User email is not verified"
        echo "   → Fix: AWS Cognito Console → Users → $USERNAME → Actions → Mark email as verified"
    fi
    if echo "$COGNITO_RESPONSE" | grep -q "PasswordResetRequiredException"; then
        echo "   → User has temporary password"
        echo "   → Fix: AWS Cognito Console → Users → $USERNAME → Actions → Set password"
    fi
    if echo "$COGNITO_RESPONSE" | grep -q "NotAuthorizedException"; then
        echo "   → Wrong password or user doesn't exist"
        echo "   → Fix: Check password or create user in Cognito"
    fi
    if echo "$COGNITO_RESPONSE" | grep -q "UserNotFoundException"; then
        echo "   → User doesn't exist"
        echo "   → Fix: Create user in Cognito Console"
    fi
fi
echo ""

# Test 3: Test Backend API Login
echo "Test 3: Testing backend API login endpoint..."
echo ""

API_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BACKEND_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

HTTP_CODE=$(echo "$API_RESPONSE" | tail -1)
BODY=$(echo "$API_RESPONSE" | head -n -1)

if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ Backend API login SUCCESSFUL"
    echo "$BODY" | grep -o '"idToken":"[^"]*' | head -1 | cut -d'"' -f4 | sed 's/^/   ID Token: /' | head -c 60
    echo "..."
elif [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ] || [ "$HTTP_CODE" = "404" ]; then
    echo "❌ Backend API login FAILED (HTTP $HTTP_CODE)"
    echo ""
    echo "   Error Message:"
    echo "$BODY" | grep -o '"message":"[^"]*' | cut -d'"' -f4 || echo "$BODY"
    echo ""
    echo "   Check backend logs for detailed error information"
else
    echo "❌ Backend API error (HTTP $HTTP_CODE)"
    echo "   Response: $BODY"
fi
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 Summary:"
echo ""
echo "If Test 2 (Direct Cognito) fails:"
echo "  → Issue is with Cognito configuration or user setup"
echo "  → Check user in Cognito Console"
echo ""
echo "If Test 2 succeeds but Test 3 fails:"
echo "  → Issue is with backend authentication flow"
echo "  → Check backend logs for detailed error"
echo ""
echo "If both succeed:"
echo "  → Login should work in the frontend!"
echo ""

