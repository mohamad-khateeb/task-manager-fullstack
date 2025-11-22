#!/bin/bash

# AWS Cognito Token Retrieval Script
# This script helps you get an ID token from AWS Cognito

# ============================================
# CONFIGURATION - UPDATE THESE VALUES
# ============================================
CLIENT_ID="5jimq4cdhoov9p9a8e4btcn99h"
USERNAME="mohamad98khateeb@gmail.com"
PASSWORD="Admin123!"
REGION="eu-north-1"

# ============================================
# SCRIPT - DON'T MODIFY BELOW
# ============================================

echo "🔐 Getting ID Token from AWS Cognito..."
echo ""

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo "❌ AWS CLI is not installed!"
    echo "   Install it: https://aws.amazon.com/cli/"
    echo "   Or use: brew install awscli (macOS)"
    exit 1
fi

# Check if configured
if ! aws sts get-caller-identity &> /dev/null; then
    echo "❌ AWS CLI is not configured!"
    echo "   Run: aws configure"
    exit 1
fi

# Validate configuration
if [ "$CLIENT_ID" = "YOUR_APP_CLIENT_ID_HERE" ]; then
    echo "❌ Please update CLIENT_ID in this script!"
    echo "   Edit get-token.sh and set your App Client ID"
    exit 1
fi

# Get the token
echo "📡 Authenticating user: $USERNAME"
echo ""

RESPONSE=$(aws cognito-idp initiate-auth \
    --region $REGION \
    --auth-flow USER_PASSWORD_AUTH \
    --client-id $CLIENT_ID \
    --auth-parameters USERNAME=$USERNAME,PASSWORD=$PASSWORD \
    2>&1)

# Check for errors
if [ $? -ne 0 ]; then
    echo "❌ Authentication failed!"
    echo ""
    echo "Error details:"
    echo "$RESPONSE"
    echo ""
    echo "💡 Common issues:"
    echo "   - Wrong username or password"
    echo "   - User email not verified"
    echo "   - Wrong CLIENT_ID"
    echo "   - Wrong REGION"
    exit 1
fi

# Extract ID token - try multiple methods
# Method 1: Use jq if available (best method)
if command -v jq &> /dev/null; then
    ID_TOKEN=$(echo "$RESPONSE" | jq -r '.AuthenticationResult.IdToken' 2>/dev/null)
fi

# Method 2: Use Python if jq not available
if [ -z "$ID_TOKEN" ] && command -v python3 &> /dev/null; then
    ID_TOKEN=$(echo "$RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin)['AuthenticationResult']['IdToken'])" 2>/dev/null)
fi

# Method 3: Use grep/sed as fallback
if [ -z "$ID_TOKEN" ]; then
    ID_TOKEN=$(echo "$RESPONSE" | grep -o '"IdToken"[[:space:]]*:[[:space:]]*"[^"]*' | sed 's/.*"IdToken"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/')
fi

# Method 4: Last resort - extract between quotes after IdToken
if [ -z "$ID_TOKEN" ]; then
    ID_TOKEN=$(echo "$RESPONSE" | sed -n 's/.*"IdToken"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
fi

if [ -z "$ID_TOKEN" ]; then
    echo "❌ Could not extract ID token from response"
    echo ""
    echo "Full response:"
    echo "$RESPONSE"
    exit 1
fi

echo "✅ Success! Your ID Token:"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "$ID_TOKEN"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📋 Copy the token above and paste it in your login form at:"
echo "   http://localhost:3000/login"
echo ""
echo "💡 Tip: You can also copy it to clipboard (macOS):"
echo "   echo '$ID_TOKEN' | pbcopy"
echo ""

