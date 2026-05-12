package com.blogapp.review.service;

import com.blogapp.review.dto.request.CreateReviewRequest;
import com.blogapp.review.dto.response.ReviewResponse;
import com.blogapp.common.dto.PageResponse;

import java.util.List;

public interface ReviewService {

    // ── Public / User endpoints ──────────────────────────────────────────────

    /** Submit a new review. Only one pending/published review per user is allowed. */
    ReviewResponse submitReview(CreateReviewRequest request, String userId, String email);

    /** List all PUBLISHED reviews (public-facing, paginated). */
    PageResponse<ReviewResponse> getPublishedReviews(int page, int size);

    /** Retrieve a single published review by id (public). */
    ReviewResponse getPublishedReviewById(String id);

    /** Fetch the current user's own reviews (all statuses). */
    List<ReviewResponse> getMyReviews(String userId);

    // ── Admin endpoints ──────────────────────────────────────────────────────

    /** List reviews filtered by status. Null status returns all. */
    PageResponse<ReviewResponse> getAdminReviews(String status, int page, int size);

    /** Full detail of any review (admin). */
    ReviewResponse getAdminReviewById(String id);

    /** Approve a PENDING review → PUBLISHED. */
    ReviewResponse approveReview(String id, String adminId);

    /** Reject a PENDING review → REJECTED. */
    ReviewResponse rejectReview(String id, String reason);

    /** Permanently delete a review. */
    void deleteReview(String id);
}
