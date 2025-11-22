# How Cognito Integration Works in Your App

This document explains how AWS Cognito is integrated into your Task Manager application.

---

## 🔗 Integration Overview

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Frontend  │────────▶│   Backend    │────────▶│   Cognito   │
│  (React)    │         │ (Spring Boot)│         │  (AWS)      │
└─────────────┘         └──────────────┘         └─────────────┘
     │                        │                        │
     │ 1. User logs in        │                        │
     │    gets token          │                        │
     │                        │                        │
     │ 2. Sends API request   │                        │
     │    with token          │                        │
     │───────────────────────▶│                        │
     │                        │                        │
     │                        │ 3. Validates token     │
     │                        │───────────────────────▶│
     │                        │                        │
     │                        │ 4. Token valid?        │
     │                        │◀───────────────────────│
     │                        │                        │
     │                        │ 5. Extract roles      │
     │                        │    (ADMIN/USER)        │
     │                        │                        │
     │ 6. Allow/Deny request  │                        │
     │◀───────────────────────│                        │
```

---

## 📁 Files Involved

### Backend Files:

#### 1. `application.yml` - Configuration
**Location**: `backend/src/main/resources/application.yml`

**What it does**:
- Tells Spring Boot where to find Cognito's public keys (JWKS URI)
- Stores Cognito configuration (Pool ID, Region, Client ID)

**Key parts**:
```yaml
security:
  oauth2:
    resourceserver:
      jwt:
        jwk-set-uri: https://cognito-idp.eu-north-1.amazonaws.com/...
        # ↑ This is where Spring Boot gets the keys to verify tokens
```

**When you update it**: After creating Cognito User Pool

---

#### 2. `SecurityConfig.java` - Security Rules
**Location**: `backend/src/main/java/com/example/taskmanager/config/SecurityConfig.java`

**What it does**:
- Configures how Spring Security validates JWT tokens
- Extracts user roles from Cognito groups
- Sets up CORS (allows frontend to call backend)

**Key parts**:

**Token Validation**:
```java
@Bean
public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    // ↑ Uses JWKS URI from application.yml to verify tokens
}
```

**Role Extraction**:
```java
private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    Object groups = jwt.getClaim("cognito:groups");
    // ↑ Gets groups from token (ADMIN, USER)
    // Converts to Spring roles (ROLE_ADMIN, ROLE_USER)
}
```

**CORS Configuration**:
```java
configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
// ↑ Allows frontend to make API calls
```

**When you update it**: Usually never, unless you change CORS settings

---

#### 3. Controllers - API Endpoints
**Locations**: 
- `ProjectController.java`
- `TaskController.java`

**What they do**:
- Define API endpoints
- Use `@PreAuthorize` to check user roles

**Example**:
```java
@PostMapping
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
// ↑ Only ADMIN or USER can create projects

@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ROLE_ADMIN')")
// ↑ Only ADMIN can delete projects
```

**How it works**:
1. User sends request with token
2. Spring Security validates token
3. Extracts roles from token
4. Checks if user has required role
5. Allows or denies request

---

### Frontend Files:

#### 1. `AuthContext.js` - Authentication State
**Location**: `frontend/src/contexts/AuthContext.js`

**What it does**:
- Stores JWT token in browser's localStorage
- Tracks if user is logged in
- Provides login/logout functions

**Key parts**:
```javascript
const [token, setToken] = useState(localStorage.getItem('cognito_id_token'));
// ↑ Gets token from browser storage on page load

const login = (idToken) => {
  localStorage.setItem('cognito_id_token', idToken);
  // ↑ Saves token when user logs in
};
```

**When you update it**: If you want to change how tokens are stored

---

#### 2. `api.js` - API Client
**Location**: `frontend/src/services/api.js`

**What it does**:
- Creates Axios instance for API calls
- Automatically adds token to every request
- Handles 401 errors (redirects to login)

**Key parts**:

**Request Interceptor**:
```javascript
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('cognito_id_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
    // ↑ Adds token to every API request
  }
  return config;
});
```

**Response Interceptor**:
```javascript
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('cognito_id_token');
      window.location.href = '/login';
      // ↑ Redirects to login if token is invalid
    }
  }
);
```

**When you update it**: If you change API base URL or add new endpoints

---

#### 3. `Login.js` - Login Component
**Location**: `frontend/src/components/Login.js`

**What it does**:
- Shows login form
- Accepts ID token (currently manual input)
- Saves token and redirects to projects

**Current implementation**:
```javascript
const handleSubmit = (e) => {
  e.preventDefault();
  if (idToken.trim()) {
    login(idToken);  // Save token
    navigate('/projects');  // Go to projects page
  }
};
```

**Future enhancement**: Could integrate Cognito Hosted UI here

---

#### 4. `App.js` - Route Protection
**Location**: `frontend/src/App.js`

**What it does**:
- Wraps app with AuthProvider
- Protects routes (redirects to login if not authenticated)

**Key parts**:
```javascript
const PrivateRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to="/login" />;
  // ↑ Only shows protected pages if user is logged in
};
```

---

## 🔄 Complete Flow Example

### Scenario: User Creates a Project

#### Step 1: User Gets Token
```
User → Cognito Hosted UI → Login → Gets ID Token
```

#### Step 2: User Logs into App
```
User → Frontend Login Page → Pastes Token → Token Saved
```

#### Step 3: User Clicks "Create Project"
```
User → Fills Form → Clicks "Create" → Frontend sends request
```

#### Step 4: Frontend Adds Token
```javascript
// In api.js
POST /api/projects
Headers: {
  Authorization: "Bearer eyJraWQiOiJcL1..."
  Content-Type: "application/json"
}
Body: { name: "My Project", description: "..." }
```

#### Step 5: Backend Receives Request
```
Spring Security intercepts request
→ Checks for Authorization header
→ Extracts token
```

#### Step 6: Backend Validates Token
```java
// In SecurityConfig
JwtDecoder validates token using JWKS URI
→ Checks signature
→ Checks expiration
→ Extracts claims (username, groups)
```

#### Step 7: Backend Checks Permissions
```java
// In ProjectController
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
→ Checks if user has ROLE_ADMIN or ROLE_USER
→ User is in ADMIN group → Has ROLE_ADMIN → ✅ Allowed
```

#### Step 8: Backend Processes Request
```java
// In ProjectService
Project created and saved to database
→ Returns project data
```

#### Step 9: Frontend Receives Response
```
Frontend receives 201 Created
→ Shows success message
→ Refreshes project list
```

---

## 🔐 Security Flow

### Token Validation Process:

1. **Token Structure** (JWT):
   ```
   Header.Payload.Signature
   ```

2. **Header** contains:
   - Algorithm used (RS256)
   - Key ID (kid)

3. **Payload** contains:
   - Username
   - `cognito:groups`: ["ADMIN", "USER"]
   - Expiration time
   - Issuer (Cognito)

4. **Signature** is:
   - Cryptographically signed by Cognito
   - Can only be verified with Cognito's public key

5. **Validation Steps**:
   ```
   Backend receives token
   → Extracts key ID from header
   → Fetches public key from JWKS URI
   → Verifies signature
   → Checks expiration
   → Extracts groups/roles
   → Allows/denies request
   ```

---

## 🎯 Role-Based Access Control

### How Roles Work:

#### In Cognito:
- User is in **ADMIN** group
- Token contains: `"cognito:groups": ["ADMIN"]`

#### In Backend:
- SecurityConfig converts: `ADMIN` → `ROLE_ADMIN`
- Controller checks: `@PreAuthorize("hasRole('ROLE_ADMIN')")`

#### Permissions:

| Action | ADMIN | USER |
|--------|-------|------|
| View projects | ✅ | ✅ |
| Create project | ✅ | ✅ |
| Update project | ✅ | ✅ |
| Delete project | ✅ | ❌ |
| View tasks | ✅ | ✅ |
| Create task | ✅ | ✅ |
| Update task | ✅ | ✅ |
| Delete task | ✅ | ❌ |

---

## 🔧 Configuration Points

### What You Configure:

1. **Cognito Setup** (AWS Console):
   - User Pool ID
   - App Client ID
   - Groups (ADMIN, USER)
   - Users

2. **Backend** (`application.yml`):
   - JWKS URI (where to get public keys)
   - User Pool ID (for reference)
   - Region (for reference)
   - App Client ID (for reference)

3. **Frontend** (`api.js`):
   - API base URL (currently `http://localhost:8080/api`)
   - Token storage (currently localStorage)

### What's Automatic:

- ✅ Token validation (handled by Spring Security)
- ✅ Role extraction (handled by SecurityConfig)
- ✅ CORS (configured in SecurityConfig)
- ✅ Token expiration (handled by Spring Security)
- ✅ 401 handling (handled by api.js interceptor)

---

## 🐛 Common Issues & Solutions

### Issue: "Invalid token"
**Cause**: JWKS URI incorrect or token expired
**Solution**: Check `application.yml` JWKS URI, get new token

### Issue: "403 Forbidden"
**Cause**: User not in required group
**Solution**: Add user to ADMIN or USER group in Cognito

### Issue: "CORS error"
**Cause**: Frontend URL not in allowed origins
**Solution**: Check `SecurityConfig.java` CORS configuration

### Issue: "Token expired"
**Cause**: Tokens expire after some time
**Solution**: Get new token, or implement token refresh

---

## 📚 Summary

**Backend**:
- Validates tokens using Cognito's public keys (JWKS)
- Extracts roles from token groups
- Enforces permissions based on roles

**Frontend**:
- Stores token in localStorage
- Adds token to every API request
- Handles authentication errors

**Cognito**:
- Issues tokens after successful login
- Includes user groups in token
- Provides public keys for token validation

**Together**:
- Secure authentication
- Role-based authorization
- Seamless user experience

---

## 🚀 Next Steps

1. Follow setup guides to create Cognito User Pool
2. Update `application.yml` with your Cognito values
3. Test authentication with provided scripts
4. (Optional) Integrate Cognito Hosted UI into frontend

Good luck! 🎉

