# AWS Cognito Setup Checklist ✅

Follow this checklist step by step. Check off each item as you complete it.

---

## 📋 Pre-Setup Checklist

- [ ] AWS account created and logged in
- [ ] Know which region you want to use (e.g., `eu-north-1`, `us-east-1`)
- [ ] Have an email address ready for testing

---

## 🏗️ Step 1: Create User Pool

### In AWS Console:
- [ ] Search for "Cognito" in AWS Console
- [ ] Click "Create user pool"
- [ ] **Sign-in options**: Select "Email" only → Next
- [ ] **Password policy**: Choose "Cognito defaults" → Next
- [ ] **MFA**: Select "No MFA" → Next
- [ ] **Self-service sign-up**: ✅ Enable → Next
- [ ] **Email provider**: Select "Send email with Cognito" → Next
- [ ] **User pool name**: Enter `task-manager-pool`
- [ ] **App client name**: Enter `task-manager-client`
- [ ] **Client secret**: ⚠️ Select "Don't generate a client secret" → Next
- [ ] Review and click "Create user pool"

**✅ User Pool Created!**

---

## 👥 Step 2: Create Groups

### In Your User Pool:
- [ ] Click "User groups" in left sidebar
- [ ] Click "Create group"
  - [ ] Name: `ADMIN` (all caps, exactly)
  - [ ] Description: "Administrator users"
  - [ ] Click "Create group"
- [ ] Click "Create group" again
  - [ ] Name: `USER` (all caps, exactly)
  - [ ] Description: "Regular users"
  - [ ] Click "Create group"

**✅ Groups Created!**

---

## 👤 Step 3: Create Users

### Create Admin User:
- [ ] Click "Users" in left sidebar
- [ ] Click "Create user"
- [ ] **User name**: `admin`
- [ ] **Email address**: Your email
- [ ] **Temporary password**: Set a password (e.g., `Admin123!`)
- [ ] ✅ **Mark email address as verified** (IMPORTANT!)
- [ ] Click "Create user"
- [ ] Click on the user you just created
- [ ] Scroll to "Group memberships"
- [ ] Click "Add user to group"
- [ ] Select "ADMIN"
- [ ] Click "Add"

**✅ Admin User Created!**

### Create Regular User:
- [ ] Go back to "Users" → "Create user"
- [ ] **User name**: `user1`
- [ ] **Email address**: Another email (can be same)
- [ ] **Temporary password**: Set a password (e.g., `User123!`)
- [ ] ✅ **Mark email address as verified**
- [ ] Click "Create user"
- [ ] Click on the user
- [ ] Add to "USER" group

**✅ Regular User Created!**

---

## 🔑 Step 4: Get Configuration Values

### Find These Values:
- [ ] **User Pool ID**: 
  - Location: Top of User Pool dashboard
  - Format: `eu-north-1_6F25VrEvR`
  - Copy: `_______________________`

- [ ] **Region**: 
  - From Pool ID (part before underscore)
  - Copy: `_______________________`

- [ ] **App Client ID**: 
  - Location: App integration → App clients
  - Copy: `_______________________`

- [ ] **JWKS URI**: 
  - Format: `https://cognito-idp.{REGION}.amazonaws.com/{POOL_ID}/.well-known/jwks.json`
  - Replace {REGION} and {POOL_ID} with your values
  - Copy: `https://cognito-idp._______.amazonaws.com/_______/.well-known/jwks.json`

**✅ Values Collected!**

---

## ⚙️ Step 5: Update Backend Configuration

### Edit File:
- [ ] Open: `backend/src/main/resources/application.yml`
- [ ] Find the `security.oauth2.resourceserver.jwt.jwk-set-uri` line
- [ ] Replace with your JWKS URI
- [ ] Find the `cognito` section
- [ ] Update `userPoolId` with your Pool ID
- [ ] Update `region` with your region
- [ ] Update `appClientId` with your Client ID
- [ ] Save the file

**✅ Configuration Updated!**

---

## 🧪 Step 6: Test Your Setup

### Option A: Using Helper Script
- [ ] Edit `get-token.sh` (or `get-token.bat` on Windows)
- [ ] Update `CLIENT_ID`, `USERNAME`, `PASSWORD`, `REGION`
- [ ] Run: `./get-token.sh` (or `get-token.bat` on Windows)
- [ ] Copy the ID token from output

### Option B: Using AWS CLI
- [ ] Install AWS CLI (if not installed)
- [ ] Configure: `aws configure`
- [ ] Run command:
  ```bash
  aws cognito-idp initiate-auth \
    --auth-flow USER_PASSWORD_AUTH \
    --client-id YOUR_CLIENT_ID \
    --auth-parameters USERNAME=admin,PASSWORD=YourPassword
  ```
- [ ] Copy `IdToken` from response

### Test in Application:
- [ ] Start backend: `cd backend && mvn spring-boot:run`
- [ ] Start frontend: `cd frontend && npm start`
- [ ] Go to: `http://localhost:3000/login`
- [ ] Paste your ID token
- [ ] Click "Sign In"
- [ ] ✅ Should see projects page!

**✅ Setup Complete!**

---

## 🎉 Success Indicators

You know it's working when:
- ✅ You can get an ID token
- ✅ You can login to the app with the token
- ✅ You can see the projects page
- ✅ You can create/edit projects (if you're ADMIN or USER)
- ✅ You can delete projects (only if you're ADMIN)

---

## 🆘 Need Help?

- Check `COGNITO_SETUP_GUIDE.md` for detailed explanations
- Check `COGNITO_QUICK_START.md` for quick reference
- Common issues are listed in the troubleshooting section

---

## 📝 Notes Section

Use this space to write down your values:

```
User Pool ID: _______________________
Region: _______________________
App Client ID: _______________________
JWKS URI: _______________________

Admin Username: _______________________
Admin Password: _______________________

User Username: _______________________
User Password: _______________________
```

