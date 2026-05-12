package com.blogapp.runningclass.controller;

import com.blogapp.common.dto.PageResponse;
import com.blogapp.runningclass.dto.request.EnrollmentRequest;
import com.blogapp.runningclass.dto.response.ClassResponse;
import com.blogapp.runningclass.dto.response.EnrollmentResponse;
import com.blogapp.runningclass.service.RunningClassService;
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
 * Public and authenticated user endpoints for running classes and enrollments.
 *
 * <pre>
 * Public (no auth):
 *   GET  /api/classes              — browse ACTIVE classes
 *   GET  /api/classes/{id}         — view a single ACTIVE class
 *
 * Authenticated user (JWT required):
 *   POST /api/classes/{id}/enroll  — enroll in a class
 *   GET  /api/classes/my-enrollments — view own enrollments
 *   POST /api/classes/enrollments/{enrollmentId}/cancel — cancel own enrollment
 * </pre>
 */
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Tag(name = "Running Classes", description = "Browse active classes and manage enrollments")
public class RunningClassController {

    private final RunningClassService classService;

    // ── Public ────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List active classes",
               description = "Returns paginated ACTIVE classes. Optionally filter by category: " +
                             "UNDERGRADUATE, POST_GRADUATE, PROFESSIONAL. No auth required.")
    public ResponseEntity<PageResponse<ClassResponse>> getActiveClasses(
            @Parameter(description = "Category filter") @RequestParam(required = false) String category,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "12") int size) {

        return ResponseEntity.ok(classService.getActiveClasses(category, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single active class",
               description = "Returns details of one ACTIVE class. Returns 404 if not active or not found.")
    public ResponseEntity<ClassResponse> getClassById(
            @Parameter(description = "Class ID") @PathVariable String id) {

        return ResponseEntity.ok(classService.getActiveClassById(id));
    }

    // ── Authenticated user ────────────────────────────────────────────────────

    @PostMapping("/{id}/enroll")
    @Operation(summary = "Enroll in a class",
               description = "Submit an enrollment request for the specified class. " +
                             "Requires login. Enrollment goes to PENDING status pending admin confirmation. " +
                             "Duplicate active enrollments are prevented.")
    public ResponseEntity<EnrollmentResponse> enroll(
            @Parameter(description = "Class ID") @PathVariable String id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody EnrollmentRequest request) {

        EnrollmentResponse response = classService.enroll(id, request, user.getId(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-enrollments")
    @Operation(summary = "My enrollments",
               description = "Returns all enrollment records for the currently logged-in user " +
                             "(includes all statuses: PENDING, CONFIRMED, REJECTED, CANCELLED).")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(classService.getMyEnrollments(user.getId()));
    }

    @PostMapping("/enrollments/{enrollmentId}/cancel")
    @Operation(summary = "Cancel my enrollment",
               description = "Cancel one of your own enrollments. Only PENDING or CONFIRMED enrollments can be cancelled.")
    public ResponseEntity<EnrollmentResponse> cancelMyEnrollment(
            @Parameter(description = "Enrollment ID") @PathVariable String enrollmentId,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(classService.cancelMyEnrollment(enrollmentId, user.getId()));
    }
}
