package com.blogapp.review.controller;

import com.blogapp.admin.entity.Admin;
import com.blogapp.common.dto.PageResponse;
import com.blogapp.review.dto.request.RejectReviewRequest;
import com.blogapp.review.dto.response.ReviewResponse;
import com.blogapp.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only review management endpoints.
 *
 * All routes require {@code ROLE_ADMIN} (enforced by class-level @PreAuthorize).
 *
 * <ul>
 *   <li>GET    /admin/api/reviews          — list reviews (optionally filtered by status)</li>
 *   <li>GET    /admin/api/reviews/{id}     — get a single review in full detail</li>
 *   <li>POST   /admin/api/reviews/{id}/approve — approve a PENDING review → PUBLISHED</li>
 *   <li>POST   /admin/api/reviews/{id}/reject  — reject a PENDING review → REJECTED</li>
 *   <li>DELETE /admin/api/reviews/{id}     — permanently delete a review</li>
 * </ul>
 */
@RestController
@RequestMapping("/admin/api/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Review Moderation",
     description = "Admin endpoints for approving, rejecting, and managing student reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "List reviews by status",
               description = "Returns a paginated list of reviews. Filter by status: PENDING, PUBLISHED, REJECTED. " +
                             "Omit status to return all reviews.")
    public ResponseEntity<PageResponse<ReviewResponse>> getAdminReviews(
            @Parameter(description = "Filter by status: PENDING, PUBLISHED, REJECTED")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(reviewService.getAdminReviews(status, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get review details",
               description = "Returns full review details including submitter email and rejection reason.")
    public ResponseEntity<ReviewResponse> getReviewById(
            @Parameter(description = "Review ID") @PathVariable String id) {

        return ResponseEntity.ok(reviewService.getAdminReviewById(id));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a review",
               description = "Approves a PENDING review. Status changes to PUBLISHED immediately.")
    public ResponseEntity<ReviewResponse> approveReview(
            @Parameter(description = "Review ID") @PathVariable String id,
            @AuthenticationPrincipal Admin admin) {

        String adminId = (admin != null) ? admin.getId() : "admin";
        return ResponseEntity.ok(reviewService.approveReview(id, adminId));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a review",
               description = "Rejects a PENDING review with a mandatory reason. " +
                             "The user is not notified automatically – wire email if needed.")
    public ResponseEntity<ReviewResponse> rejectReview(
            @Parameter(description = "Review ID") @PathVariable String id,
            @Valid @RequestBody RejectReviewRequest request) {

        return ResponseEntity.ok(reviewService.rejectReview(id, request.getReason()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review", description = "Permanently removes a review from the database.")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "Review ID") @PathVariable String id) {

        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
