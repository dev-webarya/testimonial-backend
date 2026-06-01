# Authentication API Flow

This document details how the frontend should integrate with the backend Authentication API. The backend provides three methods to authenticate: Password-based, Passwordless (OTP-only), and a secure Registration process.

---

## 1. Password Rules (Backend Enforced)
Any endpoint requiring a new password (`/register`, `/reset-password`, `/change-password`) enforces the following strict rules:
- **Minimum length**: 8 characters
- **Uppercase**: At least 1 (A-Z)
- **Lowercase**: At least 1 (a-z)
- **Numeric**: At least 1 (0-9)
- **Special Character**: At least 1 (`@$!%*?&` etc.)

---

## 2. Registration Flow (New Users)
For new users who want to create an account with a password.

### Step 1: Submit Details & Password
**POST** `/api/auth/register`
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "StrongPassword123!",
  "mobile": "1234567890"
}
```
**Response (201 Created)**: An OTP is sent to the user's email.

### Step 2: Verify Registration OTP
**POST** `/api/auth/verify-registration`
```json
{
  "email": "john@example.com",
  "otp": "123456"
}
```
**Response (200 OK)**: Returns the JWT Token. The user is now officially registered, verified, and logged in.

---

## 3. Standard Login Flow
For returning users who created an account with a password.

**POST** `/api/auth/login-password`
```json
{
  "email": "john@example.com",
  "password": "StrongPassword123!"
}
```
**Response (200 OK)**: Returns the JWT Token.
*Note*: If the user registers but hasn't verified their email yet, this endpoint will return a `403 Forbidden` with the message "Email not verified. Please verify your email first."

---

## 4. Passwordless Login Flow
For users who prefer to login via Email OTP (no password needed).

### Step 1: Request OTP
**POST** `/api/auth/start`
```json
{
  "email": "john@example.com"
}
```
**Response (200 OK)**: OTP sent to email.

### Step 2: Verify OTP
**POST** `/api/auth/verify`
```json
{
  "email": "john@example.com",
  "otp": "123456"
}
```
**Response (200 OK)**: Returns the JWT Token.

---

## 5. Forgot Password Flow

### Step 1: Request Password Reset OTP
**POST** `/api/auth/forgot-password`
```json
{
  "email": "john@example.com"
}
```
**Response (200 OK)**: OTP sent to email.

### Step 2: Submit New Password & OTP
**POST** `/api/auth/reset-password`
```json
{
  "email": "john@example.com",
  "otp": "123456",
  "newPassword": "NewStrongPassword123!"
}
```
**Response (200 OK)**: Password reset successful. User can now use `/login-password`.
