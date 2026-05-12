# Running Classes & Enrollment Module — Technical Documentation

> **Backend**: Spring Boot 3 · MongoDB · JWT Authentication  
> **Added**: May 2026

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Database Schema](#database-schema)
4. [Enrollment Workflow](#enrollment-workflow)
5. [API Reference — Public Endpoints](#api-reference--public-endpoints)
6. [API Reference — User Endpoints (Auth Required)](#api-reference--user-endpoints-auth-required)
7. [API Reference — Admin Endpoints](#api-reference--admin-endpoints)
8. [Authentication & Security](#authentication--security)
9. [Business Rules](#business-rules)
10. [Error Responses](#error-responses)
11. [Frontend Integration Guide](#frontend-integration-guide)
12. [File Structure](#file-structure)

---

## Overview

The **Running Classes** module enables iThinkLearn to publish live class offerings that students can browse and enroll in. It has two primary entities:

| Entity | Description |
|--------|-------------|
| `RunningClass` | A course offering with schedule, instructor, category, capacity |
| `Enrollment` | A student's enrollment request, with approval workflow |

**Key capabilities:**
- ✅ Anyone can **browse active classes** (public, no login required)
- ✅ Filter classes by **category**: Undergraduate, Post-Graduate, Professional
- ✅ Only **logged-in users** can submit enrollment requests
- ✅ **One active enrollment per user per class** (prevents spamming)
- ✅ Enrollments start as **PENDING** → Admin **confirms** or **rejects**
- ✅ **Capacity guard**: Admin cannot confirm beyond `maxCapacity`
- ✅ `enrolledCount` on the class is automatically maintained
- ✅ Users can **cancel** their own enrollments
- ✅ Admin has full **CRUD** for classes and full **moderation** for enrollments

---

## Architecture

```
com.blogapp.runningclass/
├── controller/
│   ├── RunningClassController.java       # /api/classes — public + user
│   └── AdminRunningClassController.java  # /admin/api/classes — admin CRUD
├── dto/
│   ├── request/
│   │   ├── ClassRequest.java             # Admin create/update class
│   │   ├── EnrollmentRequest.java        # User enrollment submission
│   │   └── RejectEnrollmentRequest.java  # Admin rejection reason
│   └── response/
│       ├── ClassResponse.java            # Class detail (includes availableSeats)
│       └── EnrollmentResponse.java       # Enrollment detail (admin vs user variants)
├── entity/
│   ├── RunningClass.java                 # MongoDB document
│   └── Enrollment.java                   # MongoDB document
├── enums/
│   ├── ClassCategory.java                # UNDERGRADUATE | POST_GRADUATE | PROFESSIONAL
│   ├── ClassStatus.java                  # ACTIVE | INACTIVE | COMPLETED | CANCELLED
│   └── EnrollmentStatus.java             # PENDING | CONFIRMED | REJECTED | CANCELLED
├── mapper/
│   └── RunningClassMapper.java           # Entity ↔ DTO conversion (both entities)
├── repository/
│   ├── RunningClassRepository.java
│   └── EnrollmentRepository.java
└── service/
    ├── RunningClassService.java
    └── impl/
        └── RunningClassServiceImpl.java
```

---

## Database Schema

### Collection: `running_classes`

| Field | Type | Notes |
|-------|------|-------|
| `_id` | ObjectId | Auto-generated |
| `title` | String | Indexed (e.g. "UG Mathematics") |
| `description` | String | Short course description |
| `category` | Enum | Indexed — `UNDERGRADUATE`, `POST_GRADUATE`, `PROFESSIONAL` |
| `schedule` | String | e.g. "Mon, Wed, Fri - 6:00 PM IST" |
| `batchSize` | String | e.g. "12-15" |
| `instructorName` | String | e.g. "Ms. Neha Aggarwal" |
| `instructorBio` | String | Optional bio |
| `feeInfo` | String | e.g. "₹5,000 / month" |
| `startDate` | LocalDateTime | Batch start |
| `endDate` | LocalDateTime | Batch end |
| `additionalInfo` | String | Notes for students |
| `status` | Enum | Indexed — `ACTIVE` (default) |
| `maxCapacity` | Integer | null = unlimited |
| `enrolledCount` | int | Auto-maintained by service |
| `createdAt` | LocalDateTime | Audit |
| `updatedAt` | LocalDateTime | Audit |

**Derived response field:** `availableSeats = maxCapacity - enrolledCount` (null if no maxCapacity)

---

### Collection: `enrollments`

| Field | Type | Notes |
|-------|------|-------|
| `_id` | ObjectId | Auto-generated |
| `classId` | String | Indexed — FK to running_classes |
| `userId` | String | Indexed — JWT User._id |
| `email` | String | Indexed — from JWT (not user input) |
| `studentName` | String | Required |
| `parentName` | String | Required |
| `mobileNumber` | String | Required, validated format |
| `gradeOrClass` | String | e.g. "B.Sc 2nd Year" |
| `schoolOrCollege` | String | Institution name |
| `preferredBatch` | String | Optional timing preference |
| `message` | String | Optional, max 1000 chars |
| `status` | Enum | Indexed — `PENDING` → `CONFIRMED`/`REJECTED`/`CANCELLED` |
| `submittedAt` | LocalDateTime | Set on creation |
| `confirmedAt` | LocalDateTime | Set on admin confirm |
| `confirmedByAdminId` | String | Admin's _id |
| `rejectionReason` | String | Set on admin reject |
| `createdAt` | LocalDateTime | Audit |
| `updatedAt` | LocalDateTime | Audit |

**Compound Index:** `{ userId, classId }` — prevents multiple active enrollments.

---

## Enrollment Workflow

```
[User clicks Enroll button]
        │
        ▼
   ┌─────────┐
   │ PENDING │  ◄─── admin sees in dashboard (default filter)
   └────┬────┘
        │
     Admin acts
      /      \
    ✅          ❌
   /              \
┌───────────┐  ┌──────────┐
│ CONFIRMED │  │ REJECTED │  ← reason required
└─────┬─────┘  └──────────┘
      │                         User may re-enroll after rejection
   User can
   cancel
      │
      ▼
┌───────────┐
│ CANCELLED │  ← also decrements enrolledCount
└───────────┘
```

**enrolledCount sync:**
- `+1` when admin confirms an enrollment
- `-1` when a CONFIRMED enrollment is cancelled or deleted

---

## API Reference — Public Endpoints

Base path: `/api/classes` — **no auth required**

---

### `GET /api/classes`

**Description:** Paginated list of ACTIVE classes. Optionally filtered by category.

**Query Parameters:**

| Param | Default | Description |
|-------|---------|-------------|
| `category` | *(all)* | `UNDERGRADUATE`, `POST_GRADUATE`, or `PROFESSIONAL` |
| `page` | `0` | Page number (0-based) |
| `size` | `12` | Page size |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "id": "66abc123",
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

---

### `GET /api/classes/{id}`

**Description:** Get a single ACTIVE class by ID.

**Response `200 OK`:** Single `ClassResponse`  
**Response `404 Not Found`:** If not found or not ACTIVE.

---

## API Reference — User Endpoints (Auth Required)

All require: `Authorization: Bearer <JWT>` (ROLE_USER)

---

### `POST /api/classes/{id}/enroll`

**Description:** Enroll in the specified class. Goes to PENDING status for admin review.

**Request Body:**
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

**Validation:**
- `studentName`, `parentName`, `mobileNumber`, `gradeOrClass`, `schoolOrCollege` — required
- `mobileNumber` — format: `^[+]?[0-9]{7,15}$`
- `message` — max 1000 chars (optional)
- `email` is taken from JWT — **not** a request field

**Response `201 Created`:** `EnrollmentResponse` with `status: "PENDING"`

**Error `400 Bad Request`:**
- Class is not ACTIVE
- User already has PENDING or CONFIRMED enrollment for this class
- Class is full (maxCapacity reached)

**Error `401 Unauthorized`:** No/invalid JWT.

---

### `GET /api/classes/my-enrollments`

**Description:** All enrollments for the logged-in user across all classes.

**Response `200 OK`:** Array of `EnrollmentResponse` (all statuses, rejectionReason visible to user).

---

### `POST /api/classes/enrollments/{enrollmentId}/cancel`

**Description:** Cancel one of your own enrollments (PENDING or CONFIRMED).

**Response `200 OK`:** Updated `EnrollmentResponse` with `status: "CANCELLED"`

**Error `400 Bad Request`:**
- Already cancelled or rejected
- Enrollment belongs to a different user

---

## API Reference — Admin Endpoints

Base path: `/admin/api/classes`  
**All require:** `Authorization: Bearer <Admin-JWT>` with `ROLE_ADMIN`

---

### Running Class CRUD

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/admin/api/classes` | List all classes (filterable by `status` and `category`) |
| `GET` | `/admin/api/classes/{id}` | Get any class |
| `POST` | `/admin/api/classes` | Create a new class |
| `PUT` | `/admin/api/classes/{id}` | Update a class |
| `DELETE` | `/admin/api/classes/{id}` | Delete a class |

**Create/Update Request Body (`ClassRequest`):**
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

**List Query Params:**
- `status`: `ACTIVE`, `INACTIVE`, `COMPLETED`, `CANCELLED`
- `category`: `UNDERGRADUATE`, `POST_GRADUATE`, `PROFESSIONAL`
- `page`, `size`

---

### Enrollment Moderation

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/admin/api/classes/enrollments` | List enrollments (filter by `classId` and/or `status`) |
| `GET` | `/admin/api/classes/enrollments/{id}` | Full enrollment detail (includes email) |
| `POST` | `/admin/api/classes/enrollments/{id}/confirm` | Confirm → CONFIRMED |
| `POST` | `/admin/api/classes/enrollments/{id}/reject` | Reject → REJECTED |
| `DELETE` | `/admin/api/classes/enrollments/{id}` | Delete permanently |

**Reject Request Body:**
```json
{
  "reason": "Batch is full. Please enroll in the next available batch."
}
```

**Confirm Response:** Includes `confirmedAt`, `confirmedByAdminId`; also updates `enrolledCount` on the class.

---

## Authentication & Security

### Security Rules Added to `SecurityConfig.java`

```java
// Running Classes — public browsing, auth-required enrollment
.requestMatchers(HttpMethod.GET, "/api/classes").permitAll()
.requestMatchers(HttpMethod.GET, "/api/classes/{id}").permitAll()
// POST /api/classes/{id}/enroll, GET /api/classes/my-enrollments,
// POST /api/classes/enrollments/{id}/cancel → require authentication
// (handled by .anyRequest().authenticated())
```

Admin routes at `/admin/api/classes/**` are covered by:
```java
@PreAuthorize("hasRole('ADMIN')")  // class-level annotation
```
Plus the existing `.requestMatchers("/api/admin/**").hasRole("ADMIN")` rule.

---

## Business Rules

| Rule | Detail |
|------|--------|
| **Enrollment requires login** | `POST /api/classes/{id}/enroll` returns 401 without valid JWT |
| **Class must be ACTIVE** | Cannot enroll in INACTIVE/COMPLETED/CANCELLED classes |
| **One active enrollment per user per class** | Cannot re-enroll unless previous is REJECTED or CANCELLED |
| **Capacity guard** | On enroll and on confirm: checks `enrolledCount < maxCapacity` |
| **enrolledCount auto-sync** | +1 on confirm, -1 on cancel/delete of confirmed enrollment |
| **Email from JWT** | `email` is taken from JWT principal, not user input — prevents impersonation |
| **Admin-only fields** | `email`, `confirmedByAdminId` omitted from user-facing responses |
| **Rejection reason visible to user** | User can see why their enrollment was rejected |
| **Class delete doesn't cascade** | Existing enrollments remain for audit trail |

---

## Error Responses

All errors follow the existing `GlobalExceptionHandler` format:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "This class is full. No more enrollments are being accepted.",
  "path": "/api/classes/66abc123/enroll",
  "timestamp": "2024-01-15T10:00:00"
}
```

| HTTP Status | Scenario |
|-------------|----------|
| `400` | Class not ACTIVE, duplicate enrollment, class full, confirm non-PENDING |
| `401` | Missing/invalid JWT on auth-required endpoints |
| `403` | Non-admin accessing `/admin/api/classes/**` |
| `404` | Class or enrollment not found, or class is not ACTIVE (public endpoint) |

---

## Frontend Integration Guide

### 1. Browse Classes with Category Filter

```javascript
// All active classes
const all = await fetch('/api/classes?page=0&size=12').then(r => r.json());

// Undergraduate only (maps to "Undergraduate" tab)
const ug = await fetch('/api/classes?category=UNDERGRADUATE').then(r => r.json());

// Post-Graduate
const pg = await fetch('/api/classes?category=POST_GRADUATE').then(r => r.json());

// Professional
const pro = await fetch('/api/classes?category=PROFESSIONAL').then(r => r.json());
```

### 2. Show "Enroll" Button Conditionally

```javascript
const token = localStorage.getItem('authToken');

function renderClassCard(cls) {
  const seatsText = cls.availableSeats != null
    ? `${cls.availableSeats} seats left`
    : 'Open enrollment';

  const enrollBtn = token
    ? `<button onclick="enroll('${cls.id}')">Enroll</button>`
    : `<button onclick="redirectToLogin()">Login to Enroll</button>`;

  return `
    <div class="class-card">
      <span class="badge">${cls.category}</span>
      <h3>${cls.title}</h3>
      <p>${cls.description}</p>
      <p>Schedule: ${cls.schedule}</p>
      <p>Batch size: ${cls.batchSize}</p>
      <p>Instructor: ${cls.instructorName}</p>
      <p>${seatsText}</p>
      ${enrollBtn}
    </div>`;
}
```

### 3. Submit Enrollment

```javascript
async function enroll(classId) {
  const token = localStorage.getItem('authToken');

  const res = await fetch(`/api/classes/${classId}/enroll`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      studentName: "Rahul Sharma",
      parentName: "Ramesh Sharma",
      mobileNumber: "+919876543210",
      gradeOrClass: "B.Sc 2nd Year",
      schoolOrCollege: "Delhi University",
      preferredBatch: "Evening 6 PM",
      message: "Looking to improve calculus skills."
    })
  });

  if (res.status === 201) {
    alert("Enrollment submitted! Pending admin confirmation.");
  } else if (res.status === 400) {
    const err = await res.json();
    alert(err.message);
  } else if (res.status === 401) {
    window.location.href = '/login';
  }
}
```

### 4. My Enrollments Page

```javascript
const myEnrollments = await fetch('/api/classes/my-enrollments', {
  headers: { Authorization: `Bearer ${token}` }
}).then(r => r.json());

// Shows PENDING, CONFIRMED, REJECTED, CANCELLED
myEnrollments.forEach(e => {
  console.log(`${e.classTitle} — ${e.status}`);
  if (e.status === 'REJECTED') console.log('Reason:', e.rejectionReason);
});
```

### 5. Admin Dashboard — Pending Enrollments

```javascript
const adminToken = localStorage.getItem('adminToken');

// Get all pending enrollments
const pending = await fetch('/admin/api/classes/enrollments?status=PENDING', {
  headers: { Authorization: `Bearer ${adminToken}` }
}).then(r => r.json());

// Confirm
await fetch(`/admin/api/classes/enrollments/${id}/confirm`, {
  method: 'POST',
  headers: { Authorization: `Bearer ${adminToken}` }
});

// Reject
await fetch(`/admin/api/classes/enrollments/${id}/reject`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${adminToken}` },
  body: JSON.stringify({ reason: "Batch is full." })
});
```

### 6. Admin Class Management

```javascript
// Create a class
await fetch('/admin/api/classes', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${adminToken}` },
  body: JSON.stringify({
    title: "GRE Preparation",
    description: "Intensive GRE verbal and quantitative reasoning",
    category: "POST_GRADUATE",
    schedule: "Sat, Sun - 6:00 PM IST",
    batchSize: "8-12",
    instructorName: "Ms. Ramya Rajamani",
    maxCapacity: 12,
    status: "ACTIVE"
  })
});

// Update status to INACTIVE
await fetch(`/admin/api/classes/${id}`, {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${adminToken}` },
  body: JSON.stringify({ ...existingClass, status: "INACTIVE" })
});
```

---

## File Structure

```
src/main/java/com/blogapp/
└── runningclass/
    ├── controller/
    │   ├── RunningClassController.java       ← /api/classes
    │   └── AdminRunningClassController.java  ← /admin/api/classes
    ├── dto/
    │   ├── request/
    │   │   ├── ClassRequest.java
    │   │   ├── EnrollmentRequest.java
    │   │   └── RejectEnrollmentRequest.java
    │   └── response/
    │       ├── ClassResponse.java
    │       └── EnrollmentResponse.java
    ├── entity/
    │   ├── RunningClass.java
    │   └── Enrollment.java
    ├── enums/
    │   ├── ClassCategory.java
    │   ├── ClassStatus.java
    │   └── EnrollmentStatus.java
    ├── mapper/
    │   └── RunningClassMapper.java
    ├── repository/
    │   ├── RunningClassRepository.java
    │   └── EnrollmentRepository.java
    └── service/
        ├── RunningClassService.java
        └── impl/
            └── RunningClassServiceImpl.java

Modified:
└── config/SecurityConfig.java   ← Added GET /api/classes permit rules
```

> **MongoDB Collections:** `running_classes`, `enrollments`  
> **No new pom.xml dependencies** required.
