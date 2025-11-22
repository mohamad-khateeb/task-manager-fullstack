# AWS Cognito Setup Guide - Step by Step

This guide will walk you through setting up AWS Cognito for your Task Manager application from scratch.

## Prerequisites
- An AWS account (free tier is sufficient)
- Basic understanding of web applications

---

## Part 1: Create AWS Cognito User Pool

### Step 1: Log into AWS Console
1. Go to [https://aws.amazon.com/console/](https://aws.amazon.com/console/)
2. Sign in with your AWS account
3. Make sure you're in the correct region (we'll use **eu-north-1** to match your config, but you can choose any)

### Step 2: Navigate to Cognito
1. In the AWS Console search bar at the top, type **"Cognito"**
2. Click on **"Amazon Cognito"** service
3. You'll see the Cognito dashboard

### Step 3: Create User Pool
1. Click the **"Create user pool"** button (big orange button)
2. You'll see a setup wizard with multiple steps

### Step 4: Configure Sign-in Experience
1. **Sign-in options**: 
   - ✅ Check **"Email"** (uncheck Username if checked)
   - Leave others unchecked
   - Click **"Next"**

### Step 5: Configure Security Requirements
1. **Password policy**:
   - Choose **"Cognito defaults"** (this is fine for development)
   - Or customize if you want specific requirements
2. **Multi-factor authentication**:
   - Select **"No MFA"** (for simplicity, you can enable later)
3. **User account recovery**:
   - Keep default: **"Enable self-service account recovery"**
   - Recovery method: **"Email only"**
4. Click **"Next"**

### Step 6: Configure Sign-up Experience
1. **Self-service sign-up**:
   - ✅ Check **"Enable self-service sign-up"** (allows creating users)
2. **Cognito-assisted verification**:
   - ✅ Check **"Cognito will send a verification email"**
3. **Required attributes**:
   - Keep **"email"** checked (it's required)
   - You can add more if needed, but email is enough
4. Click **"Next"**

### Step 7: Configure Message Delivery
1. **Email provider**:
   - Select **"Send email with Cognito"** (for development/testing)
   - Note: This has a limit of 50 emails/day. For production, use SES.
2. Click **"Next"**

### Step 8: Integrate Your App
1. **User pool name**: 
   - Enter: **"task-manager-pool"** (or any name you prefer)
2. **App client name**:
   - Enter: **"task-manager-client"** (or any name)
3. **Client secret**:
   - ⚠️ **IMPORTANT**: Select **"Don't generate a client secret"**
   - (Client secrets are for server-side apps, we need public client for web apps)
4. Click **"Next"**

### Step 9: Review and Create
1. Review all your settings
2. Click **"Create user pool"**
3. Wait a few seconds - you'll see a success message!

---

## Part 2: Create User Groups (ADMIN and USER)

### Why Groups?
Groups allow us to assign roles (ADMIN, USER) to users. The backend expects these groups.

### Step 1: Navigate to Groups
1. In your User Pool dashboard, click on **"User groups"** in the left sidebar
2. Click **"Create group"**

### Step 2: Create ADMIN Group
1. **Group name**: Enter **"ADMIN"** (exactly like this, all caps)
2. **Description**: "Administrator users with full access"
3. Click **"Create group"**

### Step 3: Create USER Group
1. Click **"Create group"** again
2. **Group name**: Enter **"USER"** (exactly like this, all caps)
3. **Description**: "Regular users with limited access"
4. Click **"Create group"**

✅ You should now see both groups listed!

---

## Part 3: Create Users Manually

### Step 1: Navigate to Users
1. In your User Pool dashboard, click **"Users"** in the left sidebar
2. Click **"Create user"**

### Step 2: Create an Admin User
1. **User name**: Enter **"admin"** (or any username)
2. **Email address**: Enter your email (e.g., `admin@example.com`)
3. **Temporary password**: 
   - Check **"Send an email invitation"** OR
   - Uncheck it and set a password manually (e.g., `Admin123!`)
4. **Mark email address as verified**: ✅ Check this box (important!)
5. Click **"Create user"**

### Step 3: Add Admin User to ADMIN Group
1. Click on the user you just created
2. Scroll down to **"Group memberships"** section
3. Click **"Add user to group"**
4. Select **"ADMIN"** from the dropdown
5. Click **"Add"**

### Step 4: Create a Regular User
1. Go back to **"Users"** → **"Create user"**
2. **User name**: Enter **"user1"** (or any username)
3. **Email address**: Enter an email (e.g., `user@example.com`)
4. **Temporary password**: Set a password (e.g., `User123!`)
5. **Mark email address as verified**: ✅ Check this box
6. Click **"Create user"**

### Step 5: Add Regular User to USER Group
1. Click on the user you just created
2. Scroll to **"Group memberships"**
3. Click **"Add user to group"**
4. Select **"USER"** from the dropdown
5. Click **"Add"**

---

## Part 4: Get Your Cognito Configuration Details

You need these values to configure your backend:

### Step 1: Get User Pool ID
1. In your User Pool dashboard, look at the top
2. You'll see **"User pool ID"** - it looks like: `eu-north-1_6F25VrEvR`
3. **Copy this value** - you'll need it!

### Step 2: Get Region
1. Look at the User Pool ID - the part before the underscore is your region
2. Example: `eu-north-1_6F25VrEvR` → Region is `eu-north-1`
3. **Copy this value**

### Step 3: Get App Client ID
1. Click **"App integration"** in the left sidebar
2. Scroll down to **"App clients and analytics"**
3. You'll see your app client listed
4. Click on the app client name
5. You'll see **"Client ID"** - it looks like: `22akbirfkau7agqnvsbk2kvjbm`
6. **Copy this value**

### Step 4: Get JWKS URI
1. Still in **"App integration"** tab
2. Scroll to **"Domain"** section
3. The JWKS URI format is:
   ```
   https://cognito-idp.{REGION}.amazonaws.com/{USER_POOL_ID}/.well-known/jwks.json
   ```
3. Replace `{REGION}` and `{USER_POOL_ID}` with your values
4. Example: `https://cognito-idp.eu-north-1.amazonaws.com/eu-north-1_6F25VrEvR/.well-known/jwks.json`
5. **Copy this full URL**

---

## Part 5: Update Your Backend Configuration

Now let's update your application to use these Cognito settings.

### Step 1: Update application.yml
Open: `backend/src/main/resources/application.yml`

Replace the existing Cognito configuration with your values:

```yaml
security:
  oauth2:
    resourceserver:
      jwt:
        jwk-set-uri: https://cognito-idp.YOUR_REGION.amazonaws.com/YOUR_USER_POOL_ID/.well-known/jwks.json

cognito:
  userPoolId: YOUR_USER_POOL_ID
  region: YOUR_REGION
  appClientId: YOUR_APP_CLIENT_ID
```

**Example** (replace with your actual values):
```yaml
security:
  oauth2:
    resourceserver:
      jwt:
        jwk-set-uri: https://cognito-idp.eu-north-1.amazonaws.com/eu-north-1_6F25VrEvR/.well-known/jwks.json

cognito:
  userPoolId: eu-north-1_6F25VrEvR
  region: eu-north-1
  appClientId: 22akbirfkau7agqnvsbk2kvjbm
```

---

## Part 6: Test Your Setup

### Option 1: Get ID Token Using AWS CLI (Recommended for Testing)

1. **Install AWS CLI** (if not installed):
   ```bash
   # macOS
   brew install awscli
   
   # Or download from: https://aws.amazon.com/cli/
   ```

2. **Configure AWS CLI**:
   ```bash
   aws configure
   ```
   - Enter your AWS Access Key ID
   - Enter your AWS Secret Access Key
   - Enter region: `eu-north-1` (or your region)
   - Enter output format: `json`

3. **Get ID Token** (for admin user):
   ```bash
   aws cognito-idp initiate-auth \
     --auth-flow USER_PASSWORD_AUTH \
     --client-id YOUR_APP_CLIENT_ID \
     --auth-parameters USERNAME=admin,PASSWORD=Admin123!
   ```
   
   Replace:
   - `YOUR_APP_CLIENT_ID` with your actual Client ID
   - `admin` with your admin username
   - `Admin123!` with your admin password

4. **Copy the ID Token** from the response (it's a long string in the `IdToken` field)

### Option 2: Use AWS Cognito Hosted UI (Easier for Testing)

1. **Set up Hosted UI Domain**:
   - Go to Cognito → Your User Pool → **"App integration"**
   - Scroll to **"Domain"** section
   - Click **"Create Cognito domain"**
   - Enter a domain prefix (e.g., `task-manager-auth`)
   - Click **"Create Cognito domain"**

2. **Configure Hosted UI**:
   - Still in **"App integration"**
   - Scroll to **"Hosted UI"** section
   - Click **"Edit"**
   - **Allowed callback URLs**: Add `http://localhost:3000`
   - **Allowed sign-out URLs**: Add `http://localhost:3000`
   - **Identity providers**: Check **"Cognito user pool"**
   - Click **"Save changes"**

3. **Get Sign-in URL**:
   - In **"Hosted UI"** section, you'll see a URL like:
     ```
     https://task-manager-auth.auth.eu-north-1.amazoncognito.com/login?client_id=YOUR_CLIENT_ID&response_type=token&redirect_uri=http://localhost:3000
     ```
   - Copy this URL and open it in your browser
   - Sign in with your user credentials
   - After login, you'll be redirected to `http://localhost:3000` with the token in the URL
   - Copy the `id_token` from the URL (it's in the hash fragment: `#id_token=...`)

### Option 3: Use Postman or Browser Console

You can also use the AWS Cognito API directly. See the "Testing Authentication" section below.

---

## Part 7: Test Authentication in Your App

### Test with Frontend (Current Setup)
1. Start your backend: `cd backend && mvn spring-boot:run`
2. Start your frontend: `cd frontend && npm start`
3. Go to `http://localhost:3000/login`
4. Paste your ID token in the login form
5. Click "Sign In"
6. You should be redirected to the projects page!

### Test with API (Using curl or Postman)

```bash
# Get ID token first (using AWS CLI or Hosted UI)
# Then use it in API calls:

# Get all projects
curl -X GET "http://localhost:8080/api/projects" \
  -H "Authorization: Bearer YOUR_ID_TOKEN_HERE"

# Create a project (requires USER or ADMIN role)
curl -X POST "http://localhost:8080/api/projects" \
  -H "Authorization: Bearer YOUR_ID_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Project",
    "description": "Testing Cognito integration"
  }'
```

---

## Troubleshooting

### Problem: "Invalid token" error
- **Solution**: Make sure your JWKS URI in `application.yml` is correct
- Verify the User Pool ID and region match

### Problem: "Access denied" or 403 error
- **Solution**: Check that your user is in the correct group (ADMIN or USER)
- Verify the group name is exactly "ADMIN" or "USER" (case-sensitive)

### Problem: Can't get ID token
- **Solution**: Make sure the user's email is verified
- Check that you're using the correct password
- For temporary passwords, you may need to change the password first

### Problem: CORS errors
- **Solution**: The backend already has CORS configured for `localhost:3000`
- If using a different port, update `SecurityConfig.java`

---

## Quick Reference: Your Cognito Values

Keep these handy:

```
User Pool ID: _______________________
Region: _______________________
App Client ID: _______________________
JWKS URI: https://cognito-idp._______.amazonaws.com/_______/.well-known/jwks.json
```

---

## Next Steps (Optional Enhancements)

1. **Integrate Cognito Hosted UI** into your React app (instead of manual token input)
2. **Add password reset functionality**
3. **Add user registration** in your frontend
4. **Use AWS SES** for production email sending
5. **Add MFA** for enhanced security

---

## Summary

✅ Created AWS Cognito User Pool
✅ Created ADMIN and USER groups
✅ Created test users and assigned them to groups
✅ Got all configuration values
✅ Updated backend configuration
✅ Tested authentication

Your Cognito setup is complete! The backend will now validate JWT tokens from Cognito, and users in the ADMIN group will have full access, while users in the USER group will have limited access.

