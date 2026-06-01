# Backend Implementation Walkthrough

All requested backend modifications have been successfully developed, integrated, and verified to compile flawlessly using Maven (`BUILD SUCCESS`).

Here is a summary of what was accomplished:

## 1. Cloudinary Upload Constraints (Ghost Entry Prevention)
- **New Feature**: Added a `DELETE /api/media` endpoint to allow the frontend to securely delete orphaned images directly from Cloudinary.
- **Robust Security**: The Cloudinary upload signature endpoint (`/api/media/signature`) now contains a strict backend guard. A registered user must *first* successfully verify their email OTP before they can request an upload signature. If they attempt to upload before verifying, they will receive a `400 Bad Request` ("Please verify your email via OTP before uploading media").

## 2. Strict Authenticated Submissions
- **Security Update**: `SecurityConfig` has been updated to remove the blog submission routes (`/api/blogs/submissions/**`) from public access. The API now correctly requires users to be authenticated (`Bearer` token) before they can begin a blog submission draft.

## 3. Subscription & Email Enhancements
- **Duplicate Prevention**: Modified `BlogSubscriptionServiceImpl` to verify if an email address is already actively subscribed. If they are, it instantly returns an error ("You are already subscribed to blog updates") without sending an OTP.
- **Confirmation Mail**: A brand new styled HTML email is now dispatched automatically via `EmailService` confirming a successful blog subscription.

## 4. OTP Standardization
- **5-Minute Rule**: Explicitly configured the OTP expiry timer in `OtpServiceImpl` to default to 5 minutes across the entire application. The resend functionality naturally invalidates older OTPs by overwriting the "latest active" OTP for that specific purpose.

## 5. System-wide Validation Consistency
- **Validation**: Added the missing `@Email(message = "Invalid email format")` constraint to `CreateBlogRequest.java` and verified that all other DTOs throughout the project (like `SubscriptionRequest`, `SubmissionRequest`, `AuthStartRequest`) correctly enforce email validation formatting.

> [!NOTE]
> All UI-level requirements—including the 5-minute countdown clock, rendering the resend buttons, and displaying the validation error responses provided by the backend—will need to be built into your frontend repository.
