# Frontend API Guide — Student Reviews & Running Classes

This document covers all endpoints for the two modules added to the backend.  
Base URL: `http://localhost:8080` (dev) — replace with production domain in production.

> **Auth header format (wherever required):**  
> `Authorization: Bearer <token>` — token is obtained from `/api/auth/verify` or `/api/auth/login-password`.

---

## Table of Contents

1. [Authentication Quick Reference](#1-authentication-quick-reference)
2. [Student Reviews](#2-student-reviews)
   - [2.1 List Published Reviews](#21-list-published-reviews)
   - [2.2 Get a Single Review](#22-get-a-single-review)
   - [2.3 Submit a Review *(login required)*](#23-submit-a-review-login-required)
   - [2.4 My Reviews *(login required)*](#24-my-reviews-login-required)
   - [2.5 Admin — List Reviews](#25-admin--list-reviews)
   - [2.6 Admin — Approve Review](#26-admin--approve-review)
   - [2.7 Admin — Reject Review](#27-admin--reject-review)
   - [2.8 Admin — Delete Review](#28-admin--delete-review)
3. [Running Classes & Enrollments](#3-running-classes--enrollments)
   - [3.1 List Active Classes](#31-list-active-classes)
   - [3.2 Get a Single Class](#32-get-a-single-class)
   - [3.3 Enroll in a Class *(login required)*](#33-enroll-in-a-class-login-required)
   - [3.4 My Enrollments *(login required)*](#34-my-enrollments-login-required)
   - [3.5 Cancel My Enrollment *(login required)*](#35-cancel-my-enrollment-login-required)
   - [3.6 Admin — List All Classes](#36-admin--list-all-classes)
   - [3.7 Admin — Create Class](#37-admin--create-class)
   - [3.8 Admin — Update Class](#38-admin--update-class)
   - [3.9 Admin — Delete Class](#39-admin--delete-class)
   - [3.10 Admin — List All Enrollments](#310-admin--list-all-enrollments)
   - [3.11 Admin — Confirm Enrollment](#311-admin--confirm-enrollment)
   - [3.12 Admin — Reject Enrollment](#312-admin--reject-enrollment)
   - [3.13 Admin — Delete Enrollment](#313-admin--delete-enrollment)
4. [Status & Enum Reference](#4-status--enum-reference)
5. [Important Notes for Frontend](#5-important-notes-for-frontend)

---

## 1. Authentication Quick Reference

User login uses OTP or password. The JWT token returned must be sent in every protected request.

### OTP Login (2-step)

**Step 1 — Send OTP**
- **URL:** `POST /api/auth/start`
- **Body:** `{ "email": "user@example.com", "isResend": false }`
- **Response:** `200 OK` → `{ "message": "OTP sent to user@example.com" }`

**Step 2 — Verify OTP & get token**
- **URL:** `POST /api/auth/verify`
- **Body:** `{ "email": "user@example.com", "otp": "123456" }`
- **Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": "664abc123",
    "email": "user@example.com",
    "name": "Rahul Sharma",
    "emailVerified": true
  }
}
```

### Password Login (1-step)
- **URL:** `POST /api/auth/login-password`
- **Body:** `{ "email": "user@example.com", "password": "yourpassword" }`
- **Response:** Same shape as above.

> Store the `token` in `localStorage` or a secure cookie. Attach it as `Authorization: Bearer <token>` to every protected request.

---

## 2. Student Reviews

### 2.1 List Published Reviews

Returns paginated, publicly visible reviews. No login required.

- **URL:** `GET /api/reviews`
- **Auth:** ❌ Not required
- **Query Params:**

| Param | Default | Description |
|-------|---------|-------------|
| `page` | `0` | Page number (0-based) |
| `size` | `10` | Results per page |

- **Response:** `200 OK`
```json
{
  "content": [
    {
      "id": "664abc123",
      "studentName": "Rahul Sharma",
      "parentName": "Ramesh Sharma",
      "gradeOrClass": "UG Mathematics",
      "reviewText": "Excellent teaching methodology! My concepts became crystal clear through ICFY classes. Highly recommended!",
      "overallRating": 5,
      "teachingQuality": 5,
      "personalAttention": 5,
      "testSystem": 5,
      "overallExperience": 5,
      "conceptClarity": 5,
      "doubtSolving": 5,
      "studyMaterial": 5,
      "improvementInConfidence": 5,
      "structuredPlanning": 5,
      "examOrientedPractice": 5,
      "reinforcementClasses": 5,
      "overallSatisfaction": 5,
      "batchSizeAdvantage": 5,
      "individualMonitoring": 5,
      "teacherExperience": 5,
      "resultImprovement": 5,
      "status": "PUBLISHED",
      "submittedAt": "2024-01-15T10:00:00",
      "publishedAt": "2024-01-15T14:30:00",
      "createdAt": "2024-01-15T10:00:00",
      "email": null,
      "rejectionReason": null,
      "approvedByAdminId": null
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 24,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

> `email`, `rejectionReason`, `approvedByAdminId` are always `null` in the public response — these are admin-only fields.

---

### 2.2 Get a Single Review

- **URL:** `GET /api/reviews/{id}`
- **Auth:** ❌ Not required
- **Response:** `200 OK` — single review object (same shape as above)
- **Error:** `404 Not Found` if the review doesn't exist or is not PUBLISHED

---

### 2.3 Submit a Review *(login required)*

A logged-in user can submit one review. It will be held as **PENDING** until an admin approves it.

- **URL:** `POST /api/reviews`
- **Auth:** ✅ Required — `Authorization: Bearer <token>`
- **Body:**
```json
{
  "studentName": "Rahul Sharma",
  "parentName": "Ramesh Sharma",
  "gradeOrClass": "UG Mathematics",
  "reviewText": "Excellent teaching methodology! My concepts became crystal clear through ICFY classes. Highly recommended!",
  "overallRating": 5,
  "teachingQuality": 5,
  "personalAttention": 5,
  "testSystem": 5,
  "overallExperience": 5,
  "conceptClarity": 5,
  "doubtSolving": 5,
  "studyMaterial": 5,
  "improvementInConfidence": 5,
  "structuredPlanning": 5,
  "examOrientedPractice": 5,
  "reinforcementClasses": 5,
  "overallSatisfaction": 5,
  "batchSizeAdvantage": 5,
  "individualMonitoring": 5,
  "teacherExperience": 5,
  "resultImprovement": 5
}
```

**Field Rules:**

| Field | Required | Validation |
|-------|----------|------------|
| `studentName` | ✅ | Non-blank |
| `parentName` | ✅ | Non-blank |
| `gradeOrClass` | ✅ | Non-blank |
| `reviewText` | ✅ | 20–2000 characters |
| `overallRating` | ✅ | Integer 1–5 |
| All 16 detailed ratings | ✅ | Integer 1–5 each |

- **Response:** `201 Created` — the new review with `"status": "PUBLISHED"` → actually `"PENDING"` on first submit
- **Error `400`:** User already has a PENDING or PUBLISHED review. They must wait for rejection before submitting again.
- **Error `401`:** Token is missing or expired.

> **UX tip:** After successful submission, show a message like *"Your review has been submitted and is pending approval."* Hide the "Write a Review" button if the user already has an active review (check via [2.4 My Reviews](#24-my-reviews-login-required)).

---

### 2.4 My Reviews *(login required)*

Returns all reviews submitted by the currently logged-in user, in all statuses.

- **URL:** `GET /api/reviews/me`
- **Auth:** ✅ Required
- **Response:** `200 OK` — array of review objects
```json
[
  {
    "id": "664abc123",
    "studentName": "Rahul Sharma",
    "gradeOrClass": "UG Mathematics",
    "status": "PENDING",
    "submittedAt": "2024-01-15T10:00:00",
    "rejectionReason": null,
    ...
  }
]
```

> Use this to check if the user already has an active review before showing the "Write a Review" form. If any item has `status === "PENDING"` or `status === "PUBLISHED"`, hide or disable the form.

---

### 2.5 Admin — List Reviews

Returns paginated reviews for the admin dashboard, filterable by status.

- **URL:** `GET /admin/api/reviews`
- **Auth:** ✅ Admin JWT required
- **Query Params:**

| Param | Default | Description |
|-------|---------|-------------|
| `status` | *(all)* | `PENDING`, `PUBLISHED`, or `REJECTED` |
| `page` | `0` | Page number |
| `size` | `10` | Page size |

- **Response:** `200 OK` — paginated list. Admin response **includes** `email`, `rejectionReason`, `approvedByAdminId`.

---

### 2.6 Admin — Approve Review

Publishes a PENDING review immediately.

- **URL:** `POST /admin/api/reviews/{id}/approve`
- **Auth:** ✅ Admin JWT required
- **Body:** None
- **Response:** `200 OK` — updated review with `"status": "PUBLISHED"` and `"publishedAt"` set
- **Error `400`:** Review is not in PENDING status

---

### 2.7 Admin — Reject Review

Rejects a PENDING review with a reason.

- **URL:** `POST /admin/api/reviews/{id}/reject`
- **Auth:** ✅ Admin JWT required
- **Body:**
```json
{
  "reason": "Review contains promotional content and does not meet our guidelines."
}
```
- **Validation:** `reason` is required (5–500 chars)
- **Response:** `200 OK` — updated review with `"status": "REJECTED"` and `"rejectionReason"` set
- **Error `400`:** Review is not in PENDING status

---

### 2.8 Admin — Delete Review

Permanently deletes a review.

- **URL:** `DELETE /admin/api/reviews/{id}`
- **Auth:** ✅ Admin JWT required
- **Response:** `204 No Content`
- **Error `404`:** Review not found

---

## 3. Running Classes & Enrollments

### 3.1 List Active Classes

Returns paginated ACTIVE classes with optional category filter. No login required.

- **URL:** `GET /api/classes`
- **Auth:** ❌ Not required
- **Query Params:**

| Param | Default | Description |
|-------|---------|-------------|
| `category` | *(all)* | `UNDERGRADUATE`, `POST_GRADUATE`, or `PROFESSIONAL` |
| `page` | `0` | Page number (0-based) |
| `size` | `12` | Results per page |

- **Response:** `200 OK`
```json
{
  "content": [
    {
      "id": "66def456",
      "title": "UG Mathematics",
      "description": "Comprehensive mathematics coverage for B.Sc and B.Tech students",
      "category": "UNDERGRADUATE",
      "schedule": "Mon, Wed, Fri - 6:00 PM IST",
      "batchSize": "12-15",
      "instructorName": "Ms. Neha Aggarwal",
      "instructorBio": null,
      "feeInfo": "₹5,000 / month",
      "startDate": null,
      "endDate": null,
      "additionalInfo": null,
      "status": "ACTIVE",
      "maxCapacity": 15,
      "enrolledCount": 8,
      "availableSeats": 7,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ],
  "page": 0,
  "size": 12,
  "totalElements": 8,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

> **Category tab mapping:**
> - "All" tab → no `category` param
> - "Undergraduate" tab → `category=UNDERGRADUATE`
> - "Post-Graduate" tab → `category=POST_GRADUATE`
> - "Professional" tab → `category=PROFESSIONAL`

> **Seats display:** Use `availableSeats` if not `null`. If `maxCapacity` is `null`, the class has no seat limit — do not show a seat count.

---

### 3.2 Get a Single Class

- **URL:** `GET /api/classes/{id}`
- **Auth:** ❌ Not required
- **Response:** `200 OK` — single class object (same shape as above)
- **Error:** `404 Not Found` if not found or not ACTIVE

---

### 3.3 Enroll in a Class *(login required)*

Submits an enrollment request for the specified class. The request starts as **PENDING** and requires admin confirmation.

- **URL:** `POST /api/classes/{id}/enroll`
- **Auth:** ✅ Required — `Authorization: Bearer <token>`
- **Body:**
```json
{
  "studentName": "Rahul Sharma",
  "parentName": "Ramesh Sharma",
  "mobileNumber": "+919876543210",
  "gradeOrClass": "B.Sc 2nd Year",
  "schoolOrCollege": "Delhi University",
  "preferredBatch": "Evening batch 6 PM",
  "message": "I want to improve my calculus skills for competitive exams."
}
```

**Field Rules:**

| Field | Required | Validation |
|-------|----------|------------|
| `studentName` | ✅ | Non-blank |
| `parentName` | ✅ | Non-blank |
| `mobileNumber` | ✅ | Format: `+919876543210` or `9876543210` (7–15 digits) |
| `gradeOrClass` | ✅ | Non-blank (e.g. `"B.Sc 2nd Year"`, `"12th Science"`) |
| `schoolOrCollege` | ✅ | Non-blank |
| `preferredBatch` | ❌ | Optional |
| `message` | ❌ | Optional, max 1000 characters |

> **Do NOT send `email` in the body.** The backend reads it automatically from your login token.

- **Response:** `201 Created`
```json
{
  "id": "66ghi789",
  "classId": "66def456",
  "classTitle": "UG Mathematics",
  "classSchedule": "Mon, Wed, Fri - 6:00 PM IST",
  "studentName": "Rahul Sharma",
  "parentName": "Ramesh Sharma",
  "mobileNumber": "+919876543210",
  "gradeOrClass": "B.Sc 2nd Year",
  "schoolOrCollege": "Delhi University",
  "preferredBatch": "Evening batch 6 PM",
  "message": "I want to improve my calculus skills for competitive exams.",
  "status": "PENDING",
  "submittedAt": "2024-01-15T10:00:00",
  "confirmedAt": null,
  "rejectionReason": null,
  "createdAt": "2024-01-15T10:00:00",
  "email": null,
  "confirmedByAdminId": null
}
```

**Errors:**

| Status | Reason |
|--------|--------|
| `400` | Class is not ACTIVE |
| `400` | User already has PENDING or CONFIRMED enrollment for this class |
| `400` | Class is full (maxCapacity reached) |
| `401` | Not logged in |
| `404` | Class not found |

> **UX tip:** Disable or hide the "Enroll" button if the user already has an active enrollment for that class (check via [3.4 My Enrollments](#34-my-enrollments-login-required)).

---

### 3.4 My Enrollments *(login required)*

Returns all enrollment records for the currently logged-in user across all classes, all statuses.

- **URL:** `GET /api/classes/my-enrollments`
- **Auth:** ✅ Required
- **Response:** `200 OK` — array of enrollment objects
```json
[
  {
    "id": "66ghi789",
    "classId": "66def456",
    "classTitle": "UG Mathematics",
    "classSchedule": "Mon, Wed, Fri - 6:00 PM IST",
    "studentName": "Rahul Sharma",
    "status": "CONFIRMED",
    "submittedAt": "2024-01-10T10:00:00",
    "confirmedAt": "2024-01-11T09:00:00",
    "rejectionReason": null,
    ...
  },
  {
    "id": "66xyz999",
    "classTitle": "GRE Preparation",
    "status": "REJECTED",
    "rejectionReason": "Batch is full. Please enroll in the next available batch.",
    ...
  }
]
```

> Users **can see `rejectionReason`** so they know why they were rejected. The `email` and `confirmedByAdminId` fields are `null` in this response.

---

### 3.5 Cancel My Enrollment *(login required)*

Cancels one of the user's own PENDING or CONFIRMED enrollments.

- **URL:** `POST /api/classes/enrollments/{enrollmentId}/cancel`
- **Auth:** ✅ Required
- **Body:** None
- **Response:** `200 OK` — updated enrollment with `"status": "CANCELLED"`
- **Error `400`:** Enrollment already cancelled/rejected, or belongs to another user

---

### 3.6 Admin — List All Classes

- **URL:** `GET /admin/api/classes`
- **Auth:** ✅ Admin JWT required
- **Query Params:**

| Param | Default | Description |
|-------|---------|-------------|
| `status` | *(all)* | `ACTIVE`, `INACTIVE`, `COMPLETED`, `CANCELLED` |
| `category` | *(all)* | `UNDERGRADUATE`, `POST_GRADUATE`, `PROFESSIONAL` |
| `page` | `0` | Page number |
| `size` | `10` | Page size |

- **Response:** `200 OK` — paginated `ClassResponse` list (includes `enrolledCount`, `availableSeats`)

---

### 3.7 Admin — Create Class

- **URL:** `POST /admin/api/classes`
- **Auth:** ✅ Admin JWT required
- **Body:**
```json
{
  "title": "UG Mathematics",
  "description": "Comprehensive mathematics coverage for B.Sc and B.Tech students",
  "category": "UNDERGRADUATE",
  "schedule": "Mon, Wed, Fri - 6:00 PM IST",
  "batchSize": "12-15",
  "instructorName": "Ms. Neha Aggarwal",
  "instructorBio": "10+ years in competitive exam coaching",
  "feeInfo": "₹5,000 / month",
  "startDate": "2024-06-01T09:00:00",
  "endDate": "2024-09-30T18:00:00",
  "additionalInfo": "Students must bring their own textbooks",
  "status": "ACTIVE",
  "maxCapacity": 15
}
```

**Required fields:** `title`, `category`, `schedule`, `instructorName`  
**Optional:** all others (dates, bio, fee, notes, capacity)  
If `status` is omitted, it defaults to `ACTIVE`.

- **Response:** `201 Created` — the created class object

---

### 3.8 Admin — Update Class

Replaces all fields of the class. Send the complete updated object.

- **URL:** `PUT /admin/api/classes/{id}`
- **Auth:** ✅ Admin JWT required
- **Body:** Same shape as [Create Class](#37-admin--create-class)
- **Response:** `200 OK` — updated class object
- **Error `404`:** Class not found

> To change a class to INACTIVE (pause enrollments without deleting), send the full body with `"status": "INACTIVE"`.

---

### 3.9 Admin — Delete Class

Permanently deletes a class. Existing enrollment records are **not** deleted (kept for audit).

- **URL:** `DELETE /admin/api/classes/{id}`
- **Auth:** ✅ Admin JWT required
- **Response:** `204 No Content`
- **Error `404`:** Class not found

---

### 3.10 Admin — List All Enrollments

Returns paginated enrollments with optional filters.

- **URL:** `GET /admin/api/classes/enrollments`
- **Auth:** ✅ Admin JWT required
- **Query Params:**

| Param | Default | Description |
|-------|---------|-------------|
| `classId` | *(all classes)* | Filter to one specific class |
| `status` | *(all)* | `PENDING`, `CONFIRMED`, `REJECTED`, `CANCELLED` |
| `page` | `0` | Page number |
| `size` | `10` | Page size |

- **Response:** `200 OK` — paginated enrollment list. Admin response includes full `email`, `confirmedByAdminId`, and `rejectionReason`.

> **Default admin view suggestion:** Load with `status=PENDING` to show the action queue first.

---

### 3.11 Admin — Confirm Enrollment

Approves a PENDING enrollment → sets to CONFIRMED. Also increments the class `enrolledCount`.

- **URL:** `POST /admin/api/classes/enrollments/{id}/confirm`
- **Auth:** ✅ Admin JWT required
- **Body:** None
- **Response:** `200 OK`
```json
{
  "id": "66ghi789",
  "status": "CONFIRMED",
  "confirmedAt": "2024-01-16T09:00:00",
  "confirmedByAdminId": "admin-001",
  ...
}
```
- **Error `400`:** Enrollment is not PENDING, or class is now full

---

### 3.12 Admin — Reject Enrollment

Rejects a PENDING enrollment with a mandatory reason.

- **URL:** `POST /admin/api/classes/enrollments/{id}/reject`
- **Auth:** ✅ Admin JWT required
- **Body:**
```json
{
  "reason": "Batch is full. Please enroll in the next available batch."
}
```
- **Validation:** `reason` is required (5–500 chars)
- **Response:** `200 OK` — enrollment with `"status": "REJECTED"` and `"rejectionReason"` set
- **Error `400`:** Enrollment is not PENDING

---

### 3.13 Admin — Delete Enrollment

Permanently deletes an enrollment record. If the enrollment was CONFIRMED, the class `enrolledCount` is automatically decremented.

- **URL:** `DELETE /admin/api/classes/enrollments/{id}`
- **Auth:** ✅ Admin JWT required
- **Response:** `204 No Content`
- **Error `404`:** Enrollment not found

---

## 4. Status & Enum Reference

### Review Status

| Value | Meaning |
|-------|---------|
| `PENDING` | Submitted, awaiting admin action |
| `PUBLISHED` | Approved — visible publicly |
| `REJECTED` | Rejected — user can re-submit |

### Class Category

| Value | Display label in UI |
|-------|---------------------|
| `UNDERGRADUATE` | Undergraduate |
| `POST_GRADUATE` | Post-Graduate |
| `PROFESSIONAL` | Professional |

### Class Status

| Value | Meaning |
|-------|---------|
| `ACTIVE` | Open for enrollment |
| `INACTIVE` | Temporarily paused |
| `COMPLETED` | Batch finished |
| `CANCELLED` | Cancelled by admin |

### Enrollment Status

| Value | Meaning |
|-------|---------|
| `PENDING` | Submitted, waiting for admin |
| `CONFIRMED` | Admin approved — student is enrolled |
| `REJECTED` | Admin rejected — user may re-enroll |
| `CANCELLED` | Cancelled by user |

---

## 5. Important Notes for Frontend

### Security
- Regular user tokens (`ROLE_USER`) **cannot** access any `/admin/api/**` route — you'll get `403 Forbidden`.
- Admin tokens are obtained separately via `/admin/auth/login` (or `/api/admin/auth/login`).
- Never expose the JWT in URLs — always use the `Authorization` header.

### One-submission rules
- **Reviews:** A user can only have one active (PENDING or PUBLISHED) review at a time. After rejection, they can submit a new one.
- **Enrollments:** A user can only have one active (PENDING or CONFIRMED) enrollment per class at a time. After rejection or cancellation, they can re-enroll.

### Email is never sent by the user
- For both reviews and enrollments, the `email` field is read from the JWT on the backend — do not include an email field in the submission form. The backend uses the logged-in user's verified email automatically.

### OTP Expiration and Resending
- All OTPs are valid for exactly **5 minutes**. To resend an OTP, set `"isResend": true` in the initial request body (e.g., when calling `/api/auth/start`). If the previous OTP is still valid and `"isResend": false`, the backend will not generate a new OTP or send a new email.

### Pagination
All paginated responses follow the same envelope:
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

### Standard Error Response
All errors return this shape:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Human-readable reason here",
  "path": "/api/classes/enroll",
  "timestamp": "2024-01-15T10:00:00"
}
```
Always check `response.message` to show a user-friendly error.

### Swagger UI
Interactive API documentation with try-it-out is available at:
```
http://localhost:8080/swagger-ui.html
```
Use this to test any endpoint directly during development.
