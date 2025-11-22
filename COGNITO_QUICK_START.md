# AWS Cognito Quick Start - Cheat Sheet

## 🎯 What You Need to Do (5 Steps)

### 1️⃣ Create User Pool
- AWS Console → Cognito → Create user pool
- Sign-in: Email only
- Name: `task-manager-pool`
- App client: `task-manager-client` (NO client secret)

### 2️⃣ Create Groups
- User groups → Create group: **ADMIN**
- User groups → Create group: **USER**

### 3️⃣ Create Users
- Users → Create user
- Admin: username `admin`, email verified ✅, add to **ADMIN** group
- User: username `user1`, email verified ✅, add to **USER** group

### 4️⃣ Get Your Values
- **User Pool ID**: Dashboard top (e.g., `eu-north-1_6F25VrEvR`)
- **Region**: First part of Pool ID (e.g., `eu-north-1`)
- **App Client ID**: App integration → App clients
- **JWKS URI**: `https://cognito-idp.{REGION}.amazonaws.com/{POOL_ID}/.well-known/jwks.json`

### 5️⃣ Update Backend Config
Edit `backend/src/main/resources/application.yml`:
```yaml
security:
  oauth2:
    resourceserver:
      jwt:
        jwk-set-uri: https://cognito-idp.YOUR_REGION.amazonaws.com/YOUR_POOL_ID/.well-known/jwks.json

cognito:
  userPoolId: YOUR_POOL_ID
  region: YOUR_REGION
  appClientId: YOUR_CLIENT_ID
```

---

## 🔑 How to Get ID Token (3 Methods)

### Method 1: AWS CLI (Easiest)
```bash
aws cognito-idp initiate-auth \
  --auth-flow USER_PASSWORD_AUTH \
  --client-id YOUR_CLIENT_ID \
  --auth-parameters USERNAME=admin,PASSWORD=YourPassword123!
```
Copy the `IdToken` from response.

### Method 2: Hosted UI (User-Friendly)
1. App integration → Domain → Create Cognito domain
2. Hosted UI → Edit → Callback URL: `http://localhost:3000`
3. Use the sign-in URL to login
4. Copy `id_token` from redirect URL

### Method 3: Browser Console (Quick Test)
Open browser console on your login page and run:
```javascript
// This is just for testing - you'll need to implement proper Cognito SDK
fetch('https://cognito-idp.YOUR_REGION.amazonaws.com/', {
  method: 'POST',
  headers: { 'Content-Type': 'application/x-amz-json-1.1', 'X-Amz-Target': 'AWSCognitoIdentityProviderService.InitiateAuth' },
  body: JSON.stringify({
    AuthFlow: 'USER_PASSWORD_AUTH',
    ClientId: 'YOUR_CLIENT_ID',
    AuthParameters: { USERNAME: 'admin', PASSWORD: 'YourPassword123!' }
  })
})
```

---

## ✅ Test Your Setup

1. **Start Backend**: `cd backend && mvn spring-boot:run`
2. **Start Frontend**: `cd frontend && npm start`
3. **Login**: Go to `http://localhost:3000/login`
4. **Paste Token**: Use ID token from Method 1 or 2 above
5. **Verify**: You should see projects page!

---

## 🐛 Common Issues

| Problem | Solution |
|---------|----------|
| Invalid token | Check JWKS URI in `application.yml` |
| 403 Forbidden | User not in ADMIN/USER group |
| Can't login | Email must be verified ✅ |
| CORS error | Backend already configured for `localhost:3000` |

---

## 📝 Your Values Template

```
User Pool ID: _______________________
Region: _______________________
App Client ID: _______________________
JWKS URI: https://cognito-idp._______.amazonaws.com/_______/.well-known/jwks.json
```

Fill this in and keep it safe!

