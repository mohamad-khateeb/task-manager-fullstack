# Manual App Client Creation (If UI Doesn't Show Secret Option)

If the AWS Console UI doesn't show the client secret option clearly, you can create it via AWS CLI instead.

## Option 1: Use AWS CLI to Create Public Client

Run this command (replace with your User Pool ID):

```bash
aws cognito-idp create-user-pool-client \
  --user-pool-id eu-north-1_6L2kxFs3u \
  --client-name task-manager-public \
  --no-generate-secret \
  --explicit-auth-flows ALLOW_USER_PASSWORD_AUTH ALLOW_REFRESH_TOKEN_AUTH \
  --region eu-north-1
```

This will create a public client (no secret) and return the Client ID.

## Option 2: Check Existing Client Settings

You can also check if your existing client has a secret:

```bash
aws cognito-idp describe-user-pool-client \
  --user-pool-id eu-north-1_6L2kxFs3u \
  --client-id je6d2346g3al3kd1pfumdb9hs \
  --region eu-north-1
```

Look for `"ClientSecret"` in the output. If it exists, you'll see it.

## Option 3: Update Existing Client to Remove Secret

Unfortunately, you **cannot** remove a secret from an existing client. You must create a new one.

