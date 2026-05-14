# Frontend API Documentation - Blog Subscriptions & User Management

This document provides details for the newly implemented endpoints for Blog Subscriptions and User Password Management.

---

## 1. Blog Subscriptions
These endpoints allow users to subscribe to email updates for new blog posts.

### Request Subscription OTP
Starts the subscription process by sending a 6-digit OTP to the user's email.
- **URL:** `/api/blogs/subscriptions/request-otp`
- **Method:** `POST`
- **Body:**
```json
{
  "email": "user@example.com",
  "isResend": false
}
```
- **Response:** `200 OK`
```json
{
  "success": true,
  "message": "OTP sent to user@example.com"
}
```

### Verify OTP & Subscribe
Completes the subscription after the user provides the OTP.
- **URL:** `/api/blogs/subscriptions/verify`
- **Method:** `POST`
- **Body:**
```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```
- **Response:** `200 OK`
```json
{
  "success": true,
  "message": "Successfully subscribed to blog updates"
}
```

### Unsubscribe
Allows users to stop receiving blog updates.
- **URL:** `/api/blogs/subscriptions/unsubscribe`
- **Method:** `POST`
- **Body:**
```json
{
  "email": "user@example.com"
}
```
- **Response:** `200 OK`

---

## 2. User Authentication (Password-Based)
In addition to the existing OTP login, users can now login using a password and manage their credentials.

### Login with Password
Authenticate a user using email and password.
- **URL:** `/api/auth/login-password`
- **Method:** `POST`
- **Body:**
```json
{
  "email": "user@example.com",
  "password": "yourpassword"
}
```
- **Response:** `200 OK` returns the standard `AuthResponse` with a JWT token.
- **Role:** Returns `ROLE_USER`. This role **cannot** access `/api/admin/**` endpoints.

### Forgot Password (Request OTP)
Starts the password reset process if a user forgot their password.
- **URL:** `/api/auth/forgot-password`
- **Method:** `POST`
- **Body:**
```json
{
  "email": "user@example.com",
  "isResend": false
}
```
- **Response:** `200 OK`
```json
{
  "message": "If that account exists, an OTP has been sent."
}
```

### Reset Password (Verify OTP)
Resets the password using the OTP sent in the previous step.
- **URL:** `/api/auth/reset-password`
- **Method:** `POST`
- **Body:**
```json
{
  "email": "user@example.com",
  "otp": "123456",
  "newPassword": "newsecurepassword"
}
```
- **Response:** `200 OK`

### Change Password (Authenticated)
Allows a logged-in user to change their password.
- **URL:** `/api/account/change-password`
- **Method:** `POST`
- **Headers:** `Authorization: Bearer <token>`
- **Body:**
```json
{
  "oldPassword": "currentpassword", 
  "newPassword": "newsecurepassword"
}
```
- **Note:** If the user hasn't set a password yet (i.e., they only ever used OTP login), `oldPassword` can be omitted or left blank.
- **Response:** `200 OK`

---

## 3. Important Notes for Frontend
1. **Security:** Regular users (`ROLE_USER`) are strictly forbidden from accessing admin resources. Any request to `/api/admin/**` with a user token will return a `403 Forbidden` error.
2. **Persistence:** The backend uses MongoDB for user and subscription data.
3. **Email Notifications:** Subscription notifications are triggered automatically whenever an admin approves a blog post in the admin panel.
4. **OTP Expiration and Resending:** All OTPs are valid for exactly **5 minutes**. To resend an OTP, set `"isResend": true` in the initial request body. If the previous OTP is still valid and `"isResend": false`, the backend will not generate a new OTP or send a new email.
