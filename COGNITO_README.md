# AWS Cognito Setup - Complete Guide

Welcome! This is your complete guide to setting up AWS Cognito for the Task Manager application.

---

## 📚 Documentation Structure

We've created several guides for different needs:

### 🎓 For Complete Beginners
**Start here if you're new to AWS Cognito:**
1. **[WHAT_IS_COGNITO.md](WHAT_IS_COGNITO.md)** - Learn what Cognito is and why we use it
   - Simple explanations
   - Real-world analogies
   - No technical jargon

### ✅ For Step-by-Step Setup
**Follow this for detailed instructions:**
2. **[COGNITO_SETUP_GUIDE.md](COGNITO_SETUP_GUIDE.md)** - Complete step-by-step guide
   - Detailed instructions with screenshots descriptions
   - Troubleshooting section
   - All configuration steps

### 📋 For Quick Reference
**Use this as a checklist:**
3. **[COGNITO_SETUP_CHECKLIST.md](COGNITO_SETUP_CHECKLIST.md)** - Quick checklist
   - Check off items as you go
   - Minimal text, maximum clarity
   - Perfect for following along

### ⚡ For Quick Start
**Use this when you know what you're doing:**
4. **[COGNITO_QUICK_START.md](COGNITO_QUICK_START.md)** - Quick reference
   - Essential commands
   - Common values
   - Fast lookup

### 🔧 For Understanding Integration
**Read this to understand how it all connects:**
5. **[HOW_IT_WORKS.md](HOW_IT_WORKS.md)** - Technical integration details
   - How backend validates tokens
   - How frontend uses tokens
   - Complete flow diagrams

---

## 🚀 Quick Start Path

### If you're completely new:
```
1. Read: WHAT_IS_COGNITO.md (5 min)
   ↓
2. Follow: COGNITO_SETUP_CHECKLIST.md (30 min)
   ↓
3. Reference: COGNITO_QUICK_START.md (as needed)
   ↓
4. Understand: HOW_IT_WORKS.md (optional, 10 min)
```

### If you're familiar with AWS:
```
1. Follow: COGNITO_QUICK_START.md (15 min)
   ↓
2. Use: get-token.sh or get-token.bat (for testing)
```

---

## 📁 Files Created

### Documentation:
- ✅ `WHAT_IS_COGNITO.md` - Beginner-friendly explanation
- ✅ `COGNITO_SETUP_GUIDE.md` - Detailed setup guide
- ✅ `COGNITO_SETUP_CHECKLIST.md` - Step-by-step checklist
- ✅ `COGNITO_QUICK_START.md` - Quick reference
- ✅ `HOW_IT_WORKS.md` - Technical integration guide
- ✅ `COGNITO_README.md` - This file

### Helper Scripts:
- ✅ `get-token.sh` - Get ID token (macOS/Linux)
- ✅ `get-token.bat` - Get ID token (Windows)

---

## 🎯 What You'll Accomplish

After following these guides, you will have:

1. ✅ Created AWS Cognito User Pool
2. ✅ Created ADMIN and USER groups
3. ✅ Created test users
4. ✅ Configured backend to use Cognito
5. ✅ Tested authentication
6. ✅ Understood how it all works

---

## 🔑 Key Concepts (Quick Reference)

| Term | What It Is | Where to Find It |
|------|-----------|------------------|
| **User Pool** | Container for all users | AWS Cognito Console |
| **User Pool ID** | Unique ID for your pool | Top of Cognito dashboard |
| **App Client** | Connection between app and Cognito | App integration section |
| **App Client ID** | Unique ID for your app | App clients list |
| **JWKS URI** | URL to verify tokens | Format: `https://cognito-idp.{region}.amazonaws.com/{poolId}/.well-known/jwks.json` |
| **ID Token** | Proof of authentication | Get via AWS CLI or Hosted UI |
| **Groups** | User roles (ADMIN, USER) | User groups section |

---

## 📝 Configuration Checklist

Before you start, make sure you have:
- [ ] AWS account (free tier is fine)
- [ ] Access to AWS Console
- [ ] Email address for testing
- [ ] Text editor to update `application.yml`

After setup, you'll need:
- [ ] User Pool ID
- [ ] Region
- [ ] App Client ID
- [ ] JWKS URI

---

## 🛠️ Setup Steps Summary

1. **Create User Pool** (5 min)
   - AWS Console → Cognito → Create user pool
   - Configure sign-in, security, app client

2. **Create Groups** (2 min)
   - Create ADMIN group
   - Create USER group

3. **Create Users** (5 min)
   - Create admin user → Add to ADMIN group
   - Create regular user → Add to USER group

4. **Get Configuration** (3 min)
   - Copy User Pool ID
   - Copy App Client ID
   - Build JWKS URI

5. **Update Backend** (2 min)
   - Edit `application.yml`
   - Update Cognito values

6. **Test** (5 min)
   - Get ID token
   - Login to app
   - Verify it works

**Total time: ~20-30 minutes**

---

## 🧪 Testing Your Setup

### Method 1: Using Helper Script
```bash
# Edit get-token.sh first with your values
./get-token.sh
# Copy the token and paste in login form
```

### Method 2: Using AWS CLI
```bash
aws cognito-idp initiate-auth \
  --auth-flow USER_PASSWORD_AUTH \
  --client-id YOUR_CLIENT_ID \
  --auth-parameters USERNAME=admin,PASSWORD=YourPassword
```

### Method 3: Using Hosted UI
1. Set up Cognito domain
2. Configure callback URL
3. Use sign-in URL
4. Copy token from redirect

---

## 🆘 Need Help?

### Common Issues:
- **"Invalid token"** → Check JWKS URI in `application.yml`
- **"403 Forbidden"** → User not in correct group
- **"Can't login"** → Email must be verified
- **"CORS error"** → Backend CORS already configured

### Where to Look:
- Detailed troubleshooting: `COGNITO_SETUP_GUIDE.md`
- Quick fixes: `COGNITO_QUICK_START.md`
- Technical details: `HOW_IT_WORKS.md`

---

## 📖 Additional Resources

- [AWS Cognito Documentation](https://docs.aws.amazon.com/cognito/)
- [Spring Security OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [JWT.io](https://jwt.io/) - Decode and inspect JWT tokens

---

## ✅ Success Checklist

You'll know everything is working when:

- [ ] You can create a User Pool in AWS
- [ ] You can create ADMIN and USER groups
- [ ] You can create users and assign them to groups
- [ ] You can get an ID token
- [ ] You can login to your app with the token
- [ ] You can create/edit projects (ADMIN and USER)
- [ ] You can delete projects (ADMIN only)
- [ ] You understand how tokens are validated

---

## 🎉 Next Steps After Setup

Once Cognito is set up:

1. **Test thoroughly** - Try all user roles
2. **Read HOW_IT_WORKS.md** - Understand the integration
3. **Optional enhancements**:
   - Integrate Cognito Hosted UI into frontend
   - Add password reset functionality
   - Add user registration
   - Implement token refresh

---

## 📞 Quick Reference Card

```
┌─────────────────────────────────────────┐
│  AWS Cognito Quick Reference            │
├─────────────────────────────────────────┤
│  User Pool ID: ___________________       │
│  Region: ___________________            │
│  App Client ID: ___________________     │
│  JWKS URI: ___________________           │
│                                         │
│  Admin User: ___________________        │
│  Admin Pass: ___________________        │
│                                         │
│  Test URL: http://localhost:3000       │
└─────────────────────────────────────────┘
```

---

## 🎓 Learning Path

**Day 1**: Setup
- Read `WHAT_IS_COGNITO.md`
- Follow `COGNITO_SETUP_CHECKLIST.md`
- Test authentication

**Day 2**: Understanding
- Read `HOW_IT_WORKS.md`
- Explore the code
- Test different user roles

**Day 3**: Enhancement
- Integrate Hosted UI
- Add features
- Deploy to production

---

Good luck with your setup! 🚀

If you get stuck, refer to the detailed guides or check the troubleshooting sections.

