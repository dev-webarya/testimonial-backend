# Student Review Module — Technical Documentation

> **Backend**: Spring Boot 3 · MongoDB · JWT Authentication  
> **Added**: May 2026

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Database Schema](#database-schema)
4. [Approval Workflow](#approval-workflow)
5. [API Reference — Public/User Endpoints](#api-reference--publicuser-endpoints)
6. [API Reference — Admin Endpoints](#api-reference--admin-endpoints)
7. [Authentication & Security](#authentication--security)
8. [Error Responses](#error-responses)
9. [Business Rules](#business-rules)
10. [Frontend Integration Guide](#frontend-integration-guide)
11. [File Structure](#file-structure)

---

## Overview

The **Student Review** module allows authenticated users to submit detailed star-rated reviews of the iThinkLearn experience. Reviews are held in **PENDING** status and must be **approved by an admin** before becoming publicly visible — exactly mirroring the Blog post approval workflow.

**Key capabilities:**
- ✅ Only **logged-in** users (JWT Bearer) may submit a review
- ✅ **One active review per user** (cannot resubmit unless previous one is rejected)
- ✅ Admin can **approve → PUBLISHED** or **reject → REJECTED** from the same dashboard pattern as Blog moderation
- ✅ Public listing shows only **PUBLISHED** reviews; sensitive fields (email) are hidden
- ✅ Logged-in users can see their **own review history** with current status

---

## Architecture

```
com.blogapp.review/
├── controller/
│   ├── ReviewController.java          # Public + auth-user endpoints
│   └── AdminReviewController.java     # Admin-only moderation
├── dto/
│   ├── request/
│   │   ├── CreateReviewRequest.java   # Submission body
│   │   └── RejectReviewRequest.java   # Rejection reason
│   └── response/
│       └── ReviewResponse.java        # Unified response DTO
├── entity/
│   └── StudentReview.java             # MongoDB document
├── enums/
│   └── ReviewStatus.java              # PENDING | PUBLISHED | REJECTED
├── mapper/
│   └── ReviewMapper.java              # Entity ↔ DTO conversion
├── repository/
│   └── StudentReviewRepository.java   # Spring Data MongoDB
└── service/
    ├── ReviewService.java             # Interface
    └── impl/
        └── ReviewServiceImpl.java     # Business logic
```

**Follows the same layered pattern as the Blog module.** No new dependencies required.

---

## Database Schema

**Collection:** `student_reviews`

| Field | Type | Notes |
|-------|------|-------|
| `_id` | ObjectId | MongoDB auto-generated |
| `userId` | String | Indexed — JWT principal's User ID |
| `studentName` | String | From form input |
| `parentName` | String | From form input |
| `email` | String | Indexed — populated from JWT claims |
| `gradeOrClass` | String | e.g. `"UG Mathematics"` |
| `reviewText` | String | 20–2000 chars |
| `overallRating` | int | 1–5 |
| `teachingQuality` | int | 1–5 |
| `personalAttention` | int | 1–5 |
| `testSystem` | int | 1–5 |
| `overallExperience` | int | 1–5 |
| `conceptClarity` | int | 1–5 |
| `doubtSolving` | int | 1–5 |
| `studyMaterial` | int | 1–5 |
| `improvementInConfidence` | int | 1–5 |
| `structuredPlanning` | int | 1–5 |
| `examOrientedPractice` | int | 1–5 |
| `reinforcementClasses` | int | 1–5 |
| `overallSatisfaction` | int | 1–5 |
| `batchSizeAdvantage` | int | 1–5 |
| `individualMonitoring` | int | 1–5 |
| `teacherExperience` | int | 1–5 |
| `resultImprovement` | int | 1–5 |
| `status` | Enum | `PENDING` \| `PUBLISHED` \| `REJECTED` — Indexed |
| `submittedAt` | LocalDateTime | Set on creation |
| `publishedAt` | LocalDateTime | Set on approval — Indexed |
| `approvedByAdminId` | String | Admin's `_id` |
| `rejectionReason` | String | Set on rejection |
| `createdAt` | LocalDateTime | MongoDB audit |
| `updatedAt` | LocalDateTime | MongoDB audit |

---

## Approval Workflow

```
[User submits review]
        │
        ▼
   ┌─────────┐
   │ PENDING │  ◄──── admin sees in dashboard
   └─────────┘
      /    \
    ✅       ❌
   /           \
┌───────────┐  ┌──────────┐
│ PUBLISHED │  │ REJECTED │
│ (visible) │  │ (hidden) │
└───────────┘  └──────────┘
```

- `PENDING` → `PUBLISHED`: `POST /admin/api/reviews/{id}/approve`
- `PENDING` → `REJECTED`: `POST /admin/api/reviews/{id}/reject` (reason required)
- Only `PENDING` reviews can be acted upon (same guard as Blog)
- After rejection, the user **may re-submit** a new review

---

## API Reference — Public/User Endpoints

Base path: `/api/reviews`

---

### `GET /api/reviews`

**Auth required:** No  
**Description:** List all PUBLISHED student reviews, paginated.

**Query Parameters:**

| Param | Default | Description |
|-------|---------|-------------|
| `page` | `0` | Page number (0-based) |
| `size` | `10` | Results per page |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "id": "6642abc123",
      "studentName": "Rahul Sharma",
      "parentName": "Ramesh Sharma",
      "gradeOrClass": "UG Mathematics",
      "reviewText": "Excellent teaching methodology!...",
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
      "createdAt": "2024-01-15T10:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

> 🔒 `email`, `rejectionReason`, `approvedByAdminId` are **null** in public responses.

---

### `GET /api/reviews/{id}`

**Auth required:** No  
**Description:** Get a single published review by ID.

**Response `200 OK`:** Single `ReviewResponse`  
**Response `404 Not Found`:** If review doesn't exist or is not PUBLISHED.

---

### `POST /api/reviews`

**Auth required:** ✅ Yes — `Authorization: Bearer <JWT>`  
**Description:** Submit a new review. Goes to PENDING status for admin approval.

**Request Body:**
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

**Validation Rules:**
- `studentName`, `parentName`, `gradeOrClass`, `reviewText` — required, non-blank
- `reviewText` — min 20 chars, max 2000 chars
- All rating fields — integer between 1 and 5 (inclusive)

**Response `201 Created`:** Full `ReviewResponse` (includes `status: "PENDING"`)

**Response `400 Bad Request`:**
```json
{
  "message": "You already have a review pending or published. You may submit a new one only after your previous review is rejected."
}
```

**Response `401 Unauthorized`:** If no valid JWT provided.

---

### `GET /api/reviews/me`

**Auth required:** ✅ Yes — `Authorization: Bearer <JWT>`  
**Description:** Returns all reviews submitted by the currently logged-in user (all statuses).

**Response `200 OK`:**
```json
[
  {
    "id": "6642abc123",
    "studentName": "Rahul Sharma",
    "status": "PENDING",
    "submittedAt": "2024-01-15T10:00:00"
  }
]
```

---

## API Reference — Admin Endpoints

Base path: `/admin/api/reviews`  
**All endpoints require:** `Authorization: Bearer <Admin-JWT>` with `ROLE_ADMIN`

---

### `GET /admin/api/reviews`

**Description:** List all reviews, optionally filtered by status.

**Query Parameters:**

| Param | Default | Description |
|-------|---------|-------------|
| `status` | *(all)* | `PENDING`, `PUBLISHED`, or `REJECTED` |
| `page` | `0` | Page number |
| `size` | `10` | Page size |

**Response `200 OK`:** Paginated `ReviewResponse` — includes `email`, `rejectionReason`, `approvedByAdminId`.

---

### `GET /admin/api/reviews/{id}`

**Description:** Full detail of any review regardless of status.

---

### `POST /admin/api/reviews/{id}/approve`

**Description:** Approve a PENDING review → PUBLISHED.

**Request Body:** None required.

**Response `200 OK`:**
```json
{
  "id": "6642abc123",
  "status": "PUBLISHED",
  "publishedAt": "2024-01-15T14:30:00",
  "approvedByAdminId": "admin-001"
}
```

**Response `400 Bad Request`:** If review is not PENDING.

---

### `POST /admin/api/reviews/{id}/reject`

**Description:** Reject a PENDING review → REJECTED.

**Request Body:**
```json
{
  "reason": "Review contains inappropriate content or promotional material."
}
```

**Validation:** `reason` is required, 5–500 characters.

---

### `DELETE /admin/api/reviews/{id}`

**Description:** Permanently delete a review.

**Response `204 No Content`**

---

## Authentication & Security

### User Authentication Flow

```
1. POST /api/auth/start   → OTP sent to email
   OR POST /api/auth/login-password
2. User receives JWT (ROLE_USER)
3. Client stores token
4. POST /api/reviews includes: Authorization: Bearer <token>
5. JwtAuthenticationFilter resolves User entity
6. @AuthenticationPrincipal User user injected into controller
```

### Security Config Rules Added

```java
// Public — anyone can browse published reviews
.requestMatchers(HttpMethod.GET, "/api/reviews").permitAll()
.requestMatchers(HttpMethod.GET, "/api/reviews/{id}").permitAll()

// POST /api/reviews and GET /api/reviews/me
// handled by .anyRequest().authenticated()
```

Admin routes at `/admin/api/reviews/**` are covered by:
```java
@PreAuthorize("hasRole('ADMIN')")  // class-level on AdminReviewController
```

---

## Error Responses

All errors follow the existing `GlobalExceptionHandler` format:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Review text must be between 20 and 2000 characters",
  "path": "/api/reviews",
  "timestamp": "2024-01-15T10:00:00"
}
```

| HTTP Status | Scenario |
|-------------|----------|
| `400` | Validation failure, already has active review, approve non-PENDING |
| `401` | Missing or invalid JWT on auth-required endpoints |
| `403` | Non-admin accessing `/admin/api/reviews/**` |
| `404` | Review not found or not published |

---

## Business Rules

| Rule | Detail |
|------|--------|
| **Auth required for submit** | `POST /api/reviews` returns 401 without valid JWT |
| **One active review per user** | Cannot submit if a PENDING or PUBLISHED review exists. User CAN re-submit after rejection. |
| **Only PENDING can be acted on** | Approve/Reject throw `400` if status is PUBLISHED or REJECTED |
| **Public listing hides PII** | `email`, `rejectionReason`, `approvedByAdminId` are null in public responses |
| **Email from JWT** | The `email` field is set from the JWT principal — not from user input |

---

## Frontend Integration Guide

### 1. Browse Published Reviews

```javascript
const res = await fetch('/api/reviews?page=0&size=9');
const data = await res.json();
// data.content[] → array of published ReviewResponse
```

### 2. Submit a Review

```javascript
const token = localStorage.getItem('authToken');

const res = await fetch('/api/reviews', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({ /* CreateReviewRequest fields */ })
});

if (res.status === 201) {
  alert("Review submitted! Pending admin approval.");
} else if (res.status === 401) {
  // redirect to login
}
```

### 3. Check if User Already Has an Active Review

```javascript
const myReviews = await fetch('/api/reviews/me', {
  headers: { 'Authorization': `Bearer ${token}` }
}).then(r => r.json());

const hasActive = myReviews.some(r =>
  r.status === 'PENDING' || r.status === 'PUBLISHED'
);
// If hasActive → show "Your review is pending/published" instead of form
```

### 4. Admin Moderation

```javascript
// List pending
fetch('/admin/api/reviews?status=PENDING', { headers: { Authorization: `Bearer ${adminToken}` } });

// Approve
fetch(`/admin/api/reviews/${id}/approve`, { method: 'POST', headers: { Authorization: `Bearer ${adminToken}` } });

// Reject
fetch(`/admin/api/reviews/${id}/reject`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${adminToken}` },
  body: JSON.stringify({ reason: "Inappropriate content" })
});
```

---

## File Structure

```
src/main/java/com/blogapp/
└── review/
    ├── controller/
    │   ├── ReviewController.java          ← /api/reviews
    │   └── AdminReviewController.java     ← /admin/api/reviews
    ├── dto/
    │   ├── request/
    │   │   ├── CreateReviewRequest.java
    │   │   └── RejectReviewRequest.java
    │   └── response/
    │       └── ReviewResponse.java
    ├── entity/
    │   └── StudentReview.java
    ├── enums/
    │   └── ReviewStatus.java
    ├── mapper/
    │   └── ReviewMapper.java
    ├── repository/
    │   └── StudentReviewRepository.java
    └── service/
        ├── ReviewService.java
        └── impl/
            └── ReviewServiceImpl.java

Modified:
└── config/SecurityConfig.java   ← Added GET /api/reviews permit rules
```

Also copy this doc to `@docs/student-review-module.md` in the project for team reference.
