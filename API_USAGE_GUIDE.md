# Blog Application — Complete API Usage Guide

Full step-by-step usage for every flow in the application with exact payloads.

> **Base URL:** `http://localhost:8080`
> **Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## Table of Contents

1. [User Flow: Browse & Read Blogs](#1-user-flow-browse--read-blogs)
2. [User Flow: Like / Dislike a Blog](#2-user-flow-like--dislike-a-blog)
3. [User Flow: Comment on a Blog](#3-user-flow-comment-on-a-blog)
4. [User Flow: Subscribe to Blog Updates](#4-user-flow-subscribe-to-blog-updates)
5. [User Flow: Write & Submit a Blog](#5-user-flow-write--submit-a-blog-3-step-otp)
6. [Admin Flow: Moderate Blogs](#6-admin-flow-moderate-blogs)
7. [Admin Flow: Moderate Comments](#7-admin-flow-moderate-comments)
8. [Admin Flow: Manage Subscribers](#8-admin-flow-manage-subscribers)

---

## 1. User Flow: Browse & Read Blogs

### 1.1 Get All Published Blogs

```
GET /api/blogs?page=0&size=10&sort=recent
```

**Query Parameters:**

| Param | Required | Description | Example |
|-------|----------|-------------|---------|
| `search` | No | Search title + excerpt | `physics` |
| `year` | No | Filter by year | `2026` |
| `month` | No | Filter by month (1–12) | `2` |
| `sort` | No | `recent` (default), `popular`, `oldest`, `most_commented` | `popular` |
| `page` | No | Page number (0-indexed) | `0` |
| `size` | No | Page size | `10` |

**Example — search + filter by year/month:**
```
GET /api/blogs?search=igcse&year=2026&month=2&sort=popular&page=0&size=10
```

**Response:**
```json
{
  "content": [
    {
      "id": "65a1b2c3d4e5f6789",
      "title": "How to Prepare for IGCSE Physics",
      "slug": "how-to-prepare-for-igcse-physics",
      "excerpt": "A comprehensive guide...",
      "featuredImageUrl": "https://example.com/img.jpg",
      "authorName": "John Doe",
      "status": "PUBLISHED",
      "publishedAt": "2026-02-10T12:00:00",
      "tags": ["physics", "igcse"],
      "likesCount": 42,
      "dislikesCount": 3,
      "commentsCount": 7,
      "viewsCount": 150
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### 1.2 Get Archive Index (Sidebar)

```
GET /api/blogs/archive
```

**Response:**
```json
[
  {
    "year": 2026,
    "months": [
      { "month": 2, "count": 7 },
      { "month": 1, "count": 12 }
    ]
  },
  {
    "year": 2025,
    "months": [
      { "month": 12, "count": 20 },
      { "month": 11, "count": 15 }
    ]
  }
]
```

### 1.3 Read a Blog (by slug)

```
GET /api/blogs/how-to-prepare-for-igcse-physics
```

**Response:**
```json
{
  "id": "65a1b2c3d4e5f6789",
  "title": "How to Prepare for IGCSE Physics",
  "slug": "how-to-prepare-for-igcse-physics",
  "excerpt": "A comprehensive guide...",
  "contentHtml": "<h2>Introduction</h2><p>Physics is...</p>",
  "contentJson": null,
  "featuredImageUrl": "https://example.com/img.jpg",
  "authorName": "John Doe",
  "authorEmail": "john@example.com",
  "status": "PUBLISHED",
  "submittedAt": "2026-02-09T10:00:00",
  "publishedAt": "2026-02-10T12:00:00",
  "rejectionReason": null,
  "tags": ["physics", "igcse"],
  "likesCount": 42,
  "dislikesCount": 3,
  "commentsCount": 7,
  "viewsCount": 151,
  "createdAt": "2026-02-09T10:00:00"
}
```

> **Note:** View count is auto-incremented each time the blog is fetched.

---

## 2. User Flow: Like / Dislike a Blog

### 2.1 Toggle a Reaction

```
POST /api/blogs/{blogId}/reaction
```

**Request Body:**
```json
{
  "reactionType": "LIKE",
  "visitorKey": "visitor-abc-123"
}
```

> `visitorKey` should be a unique token generated on the client side (e.g., from a cookie or fingerprint).

**Behavior:**
- First time LIKE → **adds** like
- LIKE again → **removes** like (toggle off)
- LIKE then DISLIKE → **switches** from like to dislike

**Response:**
```json
{
  "blogId": "65a1b2c3d4e5f6789",
  "likesCount": 43,
  "dislikesCount": 3,
  "userReaction": "LIKE",
  "action": "ADDED"
}
```

Possible `action` values: `ADDED`, `SWITCHED`, `REMOVED`

### 2.2 Get Current Reaction Status

```
GET /api/blogs/{blogId}/reaction?visitorKey=visitor-abc-123
```

**Response:**
```json
{
  "blogId": "65a1b2c3d4e5f6789",
  "likesCount": 43,
  "dislikesCount": 3,
  "userReaction": "LIKE",
  "action": null
}
```

---

## 3. User Flow: Comment on a Blog

### 3.1 Get Comments for a Blog

```
GET /api/blogs/{blogId}/comments?page=0&size=20
```

**Response:**
```json
{
  "content": [
    {
      "id": "comment-001",
      "blogId": "65a1b2c3d4e5f6789",
      "name": "Jane Doe",
      "commentText": "Great article! Very helpful.",
      "status": "VISIBLE",
      "createdAt": "2026-02-10T14:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### 3.2 Post a Comment

```
POST /api/blogs/{blogId}/comments
```

**Request Body:**
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "commentText": "Great article! Very helpful."
}
```

> **Anti-spam:** There's a hidden `website` field (honeypot). If a bot fills it in, the request is rejected. The client should include this field as hidden and empty.

**Response (201 Created):**
```json
{
  "id": "comment-002",
  "blogId": "65a1b2c3d4e5f6789",
  "name": "Jane Doe",
  "commentText": "Great article! Very helpful.",
  "status": "VISIBLE",
  "createdAt": "2026-02-11T15:00:00"
}
```

> **Rate limit:** Max 5 comments per IP per minute.

---

## 4. User Flow: Subscribe to Blog Updates

### Step 1: Start Subscription (sends OTP)

```
POST /api/blogs/subscribe/start
```

**Request Body:**
```json
{
  "email": "reader@example.com",
  "name": "Jane Doe",
  "isResend": false
}
```

**Response:**
```json
{
  "id": "sub-001",
  "email": "reader@example.com",
  "name": "Jane Doe",
  "status": "ACTIVE",
  "verified": false,
  "createdAt": "2026-02-11T15:00:00"
}
```

> An OTP email is sent to `reader@example.com`.

### Step 2: Verify OTP

```
POST /api/blogs/subscribe/verify-otp?email=reader@example.com&otp=123456
```

**Response:**
```json
{
  "id": "sub-001",
  "email": "reader@example.com",
  "name": "Jane Doe",
  "status": "ACTIVE",
  "verified": true,
  "createdAt": "2026-02-11T15:00:00"
}
```

### Unsubscribe

```
POST /api/blogs/subscribe/unsubscribe?email=reader@example.com
```

**Response:**
```json
{
  "message": "Successfully unsubscribed"
}
```

---

## 5. User Flow: Write & Submit a Blog (3-Step OTP)

This is the core submission workflow. A visitor writes a blog, provides identity (email + mobile), verifies email via OTP, and the blog goes to admin approval.

### Step 1: Start Submission (sends OTP to author email)

```
POST /api/blogs/submission/start
```

**Request Body:**
```json
{
  "title": "How to Prepare for IGCSE Physics",
  "excerpt": "A comprehensive guide to acing IGCSE Physics exams",
  "contentHtml": "<h2>Introduction</h2><p>Physics is one of the most popular IGCSE subjects...</p><h2>Study Tips</h2><ul><li>Practice past papers</li><li>Understand concepts, don't memorize</li></ul>",
  "contentJson": null,
  "featuredImageUrl": "https://example.com/physics.jpg",
  "tags": ["physics", "igcse", "exam-prep"],
  "authorName": "John Doe",
  "authorEmail": "john@example.com",
  "authorMobile": "+919876543210",
  "isResend": false
}
```

**Response:**
```json
{
  "message": "OTP has been sent to your email. Please verify to complete submission.",
  "referenceId": null,
  "step": "START",
  "email": "john@example.com"
}
```

> An OTP email is sent to `john@example.com`. The blog draft is stored temporarily on the server.

### Step 2: Verify OTP

```
POST /api/blogs/submission/verify
```

**Request Body:**
```json
{
  "email": "john@example.com",
  "otp": "482916"
}
```

**Response:**
```json
{
  "message": "OTP verified successfully. Please call finish endpoint to complete submission.",
  "referenceId": null,
  "step": "VERIFY",
  "email": "john@example.com"
}
```

> **OTP Rules:**
> - 6 digits, valid for 5 minutes
> - Max 5 wrong attempts
> - Resend cooldown: 60 seconds

### Step 3: Finish Submission (creates PENDING blog)

```
POST /api/blogs/submission/finish
```

**Request Body:**
```json
{
  "email": "john@example.com"
}
```

**Response:**
```json
{
  "message": "Thanks! Your blog is submitted and awaiting admin approval.",
  "referenceId": "65a1b2c3d4e5f6789",
  "step": "FINISH",
  "email": "john@example.com"
}
```

> The blog is now in `PENDING` status. A confirmation email is sent to the author. The `referenceId` is the blog's MongoDB ID.

---

## 6. Admin Flow: Moderate Blogs

### 6.1 View All Blogs (with status filter)

```
GET /api/admin/blogs?status=PENDING&page=0&size=10
```

**Valid status values:** `PENDING`, `PUBLISHED`, `REJECTED`, `DRAFT` (or omit for all)

**Response:**
```json
{
  "content": [
    {
      "id": "65a1b2c3d4e5f6789",
      "title": "How to Prepare for IGCSE Physics",
      "slug": "how-to-prepare-for-igcse-physics",
      "excerpt": "A comprehensive guide...",
      "contentHtml": "<h2>Introduction</h2><p>...</p>",
      "authorName": "John Doe",
      "authorEmail": "john@example.com",
      "status": "PENDING",
      "submittedAt": "2026-02-11T10:00:00",
      "publishedAt": null,
      "rejectionReason": null,
      "tags": ["physics", "igcse"],
      "likesCount": 0,
      "dislikesCount": 0,
      "commentsCount": 0,
      "viewsCount": 0,
      "createdAt": "2026-02-11T10:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### 6.2 View Blog Detail (Admin)

```
GET /api/admin/blogs/65a1b2c3d4e5f6789
```

Returns full blog detail including `authorEmail` and `authorMobile`.

### 6.3 Approve a Blog

```
POST /api/admin/blogs/65a1b2c3d4e5f6789/approve
```

**Request Body (optional):**
```json
{
  "adminId": "admin-001"
}
```

**What happens:**
- Blog status changes to `PUBLISHED`
- `publishedAt` is set to current time
- `year` and `month` fields are derived (for archive index)
- **Approval email** is sent to the author

**Response:** Returns the updated blog detail with `status: "PUBLISHED"`.

### 6.4 Reject a Blog (reason required)

```
POST /api/admin/blogs/65a1b2c3d4e5f6789/reject
```

**Request Body:**
```json
{
  "reason": "Content does not meet quality guidelines. Please add more detailed examples and proofread for grammar."
}
```

**What happens:**
- Blog status changes to `REJECTED`
- `rejectionReason` is stored
- **Rejection email** is sent to the author with the reason

**Response:** Returns the updated blog detail with `status: "REJECTED"`.

### 6.5 Edit a Blog (Admin)

```
PATCH /api/admin/blogs/65a1b2c3d4e5f6789
```

**Request Body:**
```json
{
  "title": "How to Prepare for IGCSE Physics — Updated",
  "excerpt": "Updated comprehensive guide...",
  "contentHtml": "<h2>Introduction (Updated)</h2><p>...</p>",
  "featuredImageUrl": "https://example.com/new-image.jpg",
  "tags": ["physics", "igcse", "updated"]
}
```

**Response:** Returns the updated blog detail.

---

## 7. Admin Flow: Moderate Comments

### 7.1 View Pending Comments

```
GET /api/admin/comments/pending?page=0&size=20
```

**Response:** Paginated list of comments with `status: "PENDING"`.

### 7.2 Hide a Comment

```
POST /api/admin/comments/comment-001/hide
```

**Response:**
```json
{
  "message": "Comment hidden successfully"
}
```

> The comment's status changes to `HIDDEN` and the blog's `commentsCount` is decremented.

### 7.3 Delete a Comment

```
DELETE /api/admin/comments/comment-001
```

**Response:**
```json
{
  "message": "Comment deleted successfully"
}
```

---

## 8. Admin Flow: Manage Subscribers

### View All Subscribers

```
GET /api/admin/subscribers?status=ACTIVE&page=0&size=10
```

**Valid status values:** `ACTIVE`, `UNSUBSCRIBED` (or omit for all)

**Response:**
```json
{
  "content": [
    {
      "id": "sub-001",
      "email": "reader@example.com",
      "name": "Jane Doe",
      "status": "ACTIVE",
      "verified": true,
      "createdAt": "2026-02-11T15:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

---

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────┐
│                    VISITOR FLOWS                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Browse Blogs ──→ GET /api/blogs                    │
│  Read Blog    ──→ GET /api/blogs/{slug}             │
│  Archive      ──→ GET /api/blogs/archive            │
│  Like/Dislike ──→ POST /api/blogs/{id}/reaction     │
│  Comment      ──→ POST /api/blogs/{id}/comments     │
│                                                     │
│  Subscribe:                                         │
│    1. POST /api/blogs/subscribe/start               │
│    2. POST /api/blogs/subscribe/verify-otp          │
│                                                     │
│  Submit Blog:                                       │
│    1. POST /api/blogs/submission/start  ──→ OTP     │
│    2. POST /api/blogs/submission/verify ──→ Check   │
│    3. POST /api/blogs/submission/finish ──→ PENDING │
│                                                     │
├─────────────────────────────────────────────────────┤
│                     ADMIN FLOWS                      │
├─────────────────────────────────────────────────────┤
│                                                     │
│  View Queue   ──→ GET  /api/admin/blogs?status=     │
│  Approve      ──→ POST /api/admin/blogs/{id}/approve│
│  Reject       ──→ POST /api/admin/blogs/{id}/reject │
│  Edit         ──→ PATCH /api/admin/blogs/{id}       │
│                                                     │
│  Hide Comment ──→ POST   /api/admin/comments/{}/hide│
│  Del Comment  ──→ DELETE /api/admin/comments/{}     │
│  Subscribers  ──→ GET    /api/admin/subscribers     │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## Error Responses

All errors return a standardized format:

```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2026-02-11T15:00:00",
  "errors": {
    "title": "Title is required",
    "authorEmail": "Must be a valid email address"
  }
}
```

| HTTP Code | When |
|-----------|------|
| `400` | Validation errors, bad request, invalid OTP |
| `404` | Blog/comment/subscriber not found |
| `429` | Rate limit exceeded (reactions or comments) |
| `500` | Unexpected server error |
