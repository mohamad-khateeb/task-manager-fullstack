# Troubleshooting Login Issues

## Issue: User Cannot Login

If a user cannot login, follow these steps to diagnose and fix the issue.

---

## Step 1: Check Backend Logs

When a login fails, check the backend console for detailed error messages. The improved logging will show:

- Exact error type (NotAuthorizedException, UserNotConfirmedException, etc.)
- Error code from AWS Cognito
- Full error message
- User email/username

**Look for lines like:**
```
ERROR - NotAuthorizedException for user: moveo@gmail.com - Error: ..., Error Code: ...
```

---

## Step 2: Verify User in AWS Cognito

### Check User Exists:
1. Go to AWS Cognito Console
2. Navigate to your User Pool
3. Click "Users" → Search for `moveo@gmail.com`
4. Verify the user exists

### Check Email Verification:
1. Click on the user
2. Look at "Email verified" status
3. **If NOT verified**: Click "Actions" → "Mark email as verified"
4. This is REQUIRED for USER_PASSWORD_AUTH flow

### Check User Group:
1. Scroll to "Group memberships" section
2. Verify user is in "USER" group
3. If not, click "Add user to group" → Select "USER"

### Check Password Status:
1. Look at "User status"
2. If status is "FORCE_CHANGE_PASSWORD":
   - User has temporary password
   - They need to change password first
   - Or set permanent password in Cognito

---

## Step 3: Test Authentication Directly

### Using AWS CLI:

```bash
aws cognito-idp initiate-auth \
  --region eu-north-1 \
  --auth-flow USER_PASSWORD_AUTH \
  --client-id 5jimq4cdhoov9p9a8e4btcn99h \
  --auth-parameters USERNAME=moveo@gmail.com,PASSWORD=User123!
```

**Common Responses:**

#### Success:
```json
{
  "AuthenticationResult": {
    "IdToken": "...",
    "AccessToken": "...",
    "RefreshToken": "..."
  }
}
```

#### Error: User Not Confirmed
```json
{
  "__type": "UserNotConfirmedException",
  "message": "User is not confirmed."
}
```
**Fix**: Mark email as verified in Cognito Console

#### Error: Password Reset Required
```json
{
  "__type": "PasswordResetRequiredException",
  "message": "Password reset required for the user"
}
```
**Fix**: Set permanent password in Cognito Console

#### Error: Not Authorized
```json
{
  "__type": "NotAuthorizedException",
  "message": "Incorrect username or password."
}
```
**Fix**: Check password is correct, or reset password

---

## Step 4: Common Issues and Fixes

### Issue 1: "User account is not confirmed"

**Cause**: Email not verified in Cognito

**Fix**:
1. AWS Cognito Console → Users → Select user
2. Click "Actions" → "Mark email as verified"
3. Try login again

---

### Issue 2: "Temporary password detected"

**Cause**: User was created with temporary password

**Fix Option A** (Set Permanent Password):
1. AWS Cognito Console → Users → Select user
2. Click "Actions" → "Set password"
3. Enter new permanent password
4. Uncheck "Send email notification" (optional)
5. Click "Set password"
6. Try login again

**Fix Option B** (User Changes Password):
- User needs to complete password change flow
- This requires additional implementation (not in current scope)

---

### Issue 3: "Invalid email or password"

**Possible Causes**:
1. Wrong password
2. Wrong email/username
3. User doesn't exist

**Fix**:
1. Verify email is correct
2. Verify password is correct
3. Check user exists in Cognito
4. Try resetting password if needed

---

### Issue 4: "User not found"

**Cause**: User doesn't exist in Cognito User Pool

**Fix**:
1. Create user in Cognito Console
2. Set password
3. Mark email as verified
4. Add to USER group
5. Try login again

---

## Step 5: Test with Diagnostic Endpoint

The backend now includes a diagnostic endpoint:

```bash
curl http://localhost:8080/api/auth/diagnostic
```

**Expected Response:**
```json
{
  "status": "ok",
  "message": "Authentication endpoint is available",
  "endpoint": "/api/auth/login",
  "method": "POST"
}
```

This confirms the backend is running and the endpoint is accessible.

---

## Step 6: Test Login Flow

### Test with curl:

```bash
# Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "moveo@gmail.com",
    "password": "User123!"
  }'
```

**Success Response:**
```json
{
  "idToken": "eyJraWQ...",
  "accessToken": "...",
  "refreshToken": "...",
  "expiresIn": 3600
}
```

**Error Response:**
```json
{
  "message": "User account is not confirmed. Please verify your email address in AWS Cognito."
}
```

---

## Quick Checklist for User: moveo@gmail.com

- [ ] User exists in Cognito User Pool
- [ ] Email is marked as verified ✅
- [ ] User is in USER group
- [ ] Password is permanent (not temporary)
- [ ] Password is correct: `User123!`
- [ ] App Client has USER_PASSWORD_AUTH flow enabled
- [ ] Backend is running on port 8080
- [ ] Backend logs show detailed error (if login fails)

---

## Most Common Fix

**90% of login issues are caused by unverified email.**

**Quick Fix:**
1. AWS Cognito Console
2. Users → Find `moveo@gmail.com`
3. Actions → Mark email as verified ✅
4. Try login again

---

## Getting Help

If login still fails after checking all above:

1. **Check backend logs** - Look for ERROR messages
2. **Check error message** - The improved error messages will tell you exactly what's wrong
3. **Test with AWS CLI** - Verify credentials work outside the app
4. **Verify Cognito configuration** - Check User Pool ID, Client ID, Region

The improved error handling will now show you exactly what's wrong!

