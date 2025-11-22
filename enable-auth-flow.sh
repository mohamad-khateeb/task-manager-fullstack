#!/bin/bash
# Enable USER_PASSWORD_AUTH flow for the app client

aws cognito-idp update-user-pool-client \
  --user-pool-id eu-north-1_6L2kxFs3u \
  --client-id 5jimq4cdhoov9p9a8e4btcn99h \
  --explicit-auth-flows ALLOW_USER_PASSWORD_AUTH ALLOW_REFRESH_TOKEN_AUTH \
  --region eu-north-1

echo "✅ Authentication flow enabled!"
