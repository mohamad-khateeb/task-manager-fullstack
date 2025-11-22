# AWS CLI Configuration Guide

## Step 1: Get Your AWS Access Keys

### In AWS Console:

1. **Click on your username** (top right corner of AWS Console)
2. Click **"Security credentials"** (or go directly to: https://console.aws.amazon.com/iam/home#/security_credentials)
3. Scroll down to **"Access keys"** section
4. Click **"Create access key"**
5. Choose **"Command Line Interface (CLI)"** as the use case
6. Check the confirmation box
7. Click **"Next"**
8. (Optional) Add a description like "For Task Manager CLI"
9. Click **"Create access key"**
10. **IMPORTANT**: You'll see:
    - **Access key ID** - Copy this immediately!
    - **Secret access key** - Copy this immediately! (You can only see it once)
11. Click **"Done"**

⚠️ **Save these keys securely!** You won't be able to see the secret key again.

---

## Step 2: Configure AWS CLI

Run this command:
```bash
aws configure
```

You'll be asked 4 questions. Here's what to enter:

### Question 1: AWS Access Key ID
```
AWS Access Key ID [None]: 
```
**Answer**: Paste your Access Key ID (from Step 1)
- Example: `AKIAIOSFODNN7EXAMPLE`

### Question 2: AWS Secret Access Key
```
AWS Secret Access Key [None]: 
```
**Answer**: Paste your Secret Access Key (from Step 1)
- Example: `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY`
- Note: The text won't show as you type (for security)

### Question 3: Default region name
```
Default region name [None]: 
```
**Answer**: `eu-north-1`
- This matches your Cognito region

### Question 4: Default output format
```
Default output format [None]: 
```
**Answer**: `json`
- This is the standard format

---

## Example Session

Here's what it looks like:

```bash
$ aws configure
AWS Access Key ID [None]: AKIAIOSFODNN7EXAMPLE
AWS Secret Access Key [None]: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
Default region name [None]: eu-north-1
Default output format [None]: json
```

After this, you're done! ✅

---

## Verify Configuration

Test that it works:
```bash
aws sts get-caller-identity
```

You should see your AWS account info. If you see an error, check your keys.

---

## Alternative: Use Environment Variables

If you prefer not to use `aws configure`, you can set environment variables:

```bash
export AWS_ACCESS_KEY_ID="your-access-key-id"
export AWS_SECRET_ACCESS_KEY="your-secret-access-key"
export AWS_DEFAULT_REGION="eu-north-1"
```

But `aws configure` is easier and more permanent.

---

## Security Note

- Never share your access keys
- Never commit them to Git
- If you accidentally share them, delete and create new ones immediately
- Consider using IAM users with limited permissions (not your root account)

