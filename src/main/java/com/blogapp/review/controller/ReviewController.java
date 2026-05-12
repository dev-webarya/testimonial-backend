package com.blogapp.review.controller;

import com.blogapp.common.dto.PageResponse;
import com.blogapp.review.dto.request.CreateReviewRequest;
import com.blogapp.review.dto.response.ReviewResponse;
import com.blogapp.review.service.ReviewService;
import com.blogapp.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public-facing review endpoints.
 *
 * <ul>
 *   <li>GET  /api/reviews          — anyone can browse published reviews</li>
 *   <li>GET  /api/reviews/{id}     — anyone can view a single published review</li>
 *   <li>POST /api/reviews          — AUTHENTICATED users only: submit a review</li>
 *   <li>GET  /api/reviews/me       — AUTHENTICATED users only: view own reviews</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Student Reviews", description = "Submit and browse student reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // ── Public ────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List published reviews",
               description = "Returns a paginated list of all PUBLISHED student reviews. No auth required.")
    public ResponseEntity<PageResponse<ReviewResponse>> getPublishedReviews(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(reviewService.getPublishedReviews(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a published review by ID",
               description = "Returns a single PUBLISHED review. Returns 404 for non-published reviews.")
    public ResponseEntity<ReviewResponse> getPublishedReviewById(
            @Parameter(description = "Review ID") @PathVariable String id) {

        return ResponseEntity.ok(reviewService.getPublishedReviewById(id));
    }

    // ── Authenticated user ────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Submit a review",
               description = "Logged-in users can submit a student review. Submitted reviews go into " +
                             "PENDING status and are visible to admin for moderation. " +
                             "Only one active (pending or published) review per user is allowed.")
    public ResponseEntity<ReviewResponse> submitReview(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateReviewRequest request) {

        ReviewResponse response = reviewService.submitReview(request, user.getId(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "My reviews",
               description = "Returns all reviews submitted by the currently authenticated user " +
                             "(includes PENDING, PUBLISHED, and REJECTED).")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(reviewService.getMyReviews(user.getId()));
    }
}
