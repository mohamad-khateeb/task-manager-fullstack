# Fix Login Issue for moveo@gmail.com

## 🔍 Diagnosis Results

**Issue Found**: User has a **temporary password**

**Evidence**:
- Cognito returns: `"ChallengeName": "NEW_PASSWORD_REQUIRED"`
- Email is verified: ✅ `"email_verified":"true"`
- User exists and is in USER group: ✅

**Root Cause**: When a user is created in Cognito, they often get a temporary password that must be changed before they can login with USER_PASSWORD_AUTH flow.

---

## ✅ Solution: Set Permanent Password

### Option 1: Set Password in Cognito Console (Recommended)

1. **Go to AWS Cognito Console**
   - Navigate to your User Pool: `eu-north-1_6L2kxFs3u`
   - Click "Users" in left sidebar
   - Search for: `moveo@gmail.com`
   - Click on the user

2. **Set Permanent Password**
   - Click "Actions" button (top right)
   - Select "Set password"
   - Enter password: `User123!` (or your desired password)
   - **Uncheck** "Send email notification" (optional)
   - Click "Set password"

3. **Verify User Status**
   - After setting password, user status should change from "FORCE_CHANGE_PASSWORD" to "CONFIRMED"
   - Email should be verified ✅
   - User should be in USER group ✅

4. **Test Login**
   - Try login again with email: `moveo@gmail.com`
   - Password: `User123!` (the password you just set)

---

### Option 2: Using AWS CLI

```bash
aws cognito-idp admin-set-user-password \
  --user-pool-id eu-north-1_6L2kxFs3u \
  --username moveo@gmail.com \
  --password User123! \
  --permanent \
  --region eu-north-1
```

This sets a permanent password without requiring the user to change it.

---

## 🧪 Verify the Fix

After setting the permanent password, test login:

```bash
aws cognito-idp initiate-auth \
  --region eu-north-1 \
  --auth-flow USER_PASSWORD_AUTH \
  --client-id 5jimq4cdhoov9p9a8e4btcn99h \
  --auth-parameters USERNAME=moveo@gmail.com,PASSWORD=User123!
```

**Expected Success Response:**
```json
{
  "AuthenticationResult": {
    "IdToken": "eyJraWQ...",
    "AccessToken": "...",
    "RefreshToken": "..."
  }
}
```

If you see `AuthenticationResult` instead of `ChallengeName`, the fix worked! ✅

---

## 📝 What Happened?

When you create a user manually in Cognito:
- Cognito often assigns a temporary password
- User status becomes: `FORCE_CHANGE_PASSWORD`
- USER_PASSWORD_AUTH flow requires permanent password
- User must change password before login

**Solution**: Set a permanent password in Cognito Console, then login will work.

---

## ✅ After Fixing

Once you set the permanent password:
1. User status changes to `CONFIRMED`
2. Login will work with the permanent password
3. The improved error handling will no longer show the temporary password error

---

## Quick Command to Fix

```bash
aws cognito-idp admin-set-user-password \
  --user-pool-id eu-north-1_6L2kxFs3u \
  --username moveo@gmail.com \
  --password User123! \
  --permanent \
  --region eu-north-1
```

Then test login - it should work! 🎉

