package com.blogapp.runningclass.controller;

import com.blogapp.admin.entity.Admin;
import com.blogapp.common.dto.PageResponse;
import com.blogapp.runningclass.dto.request.ClassRequest;
import com.blogapp.runningclass.dto.request.RejectEnrollmentRequest;
import com.blogapp.runningclass.dto.response.ClassResponse;
import com.blogapp.runningclass.dto.response.EnrollmentResponse;
import com.blogapp.runningclass.service.RunningClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only endpoints for managing running classes and student enrollments.
 *
 * All routes under {@code /admin/api/classes} require ROLE_ADMIN.
 *
 * <pre>
 * Classes:
 *   GET    /admin/api/classes               — list all classes
 *   GET    /admin/api/classes/{id}          — get one class
 *   POST   /admin/api/classes               — create a class
 *   PUT    /admin/api/classes/{id}          — update a class
 *   DELETE /admin/api/classes/{id}          — delete a class
 *
 * Enrollments:
 *   GET    /admin/api/classes/enrollments           — list all enrollments
 *   GET    /admin/api/classes/enrollments/{id}      — get one enrollment
 *   POST   /admin/api/classes/enrollments/{id}/confirm — confirm
 *   POST   /admin/api/classes/enrollments/{id}/reject  — reject
 *   DELETE /admin/api/classes/enrollments/{id}      — delete
 * </pre>
 */
@RestController
@RequestMapping("/admin/api/classes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Class & Enrollment Management",
     description = "Admin CRUD for running classes and enrollment moderation")
public class AdminRunningClassController {

    private final RunningClassService classService;

    // ── Classes ───────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List all classes",
               description = "Returns paginated classes with optional filters for status and category.")
    public ResponseEntity<PageResponse<ClassResponse>> getAdminClasses(
            @Parameter(description = "Filter by status: ACTIVE, INACTIVE, COMPLETED, CANCELLED")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by category: UNDERGRADUATE, POST_GRADUATE, PROFESSIONAL")
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(classService.getAdminClasses(status, category, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get class by ID")
    public ResponseEntity<ClassResponse> getClassById(
            @Parameter(description = "Class ID") @PathVariable String id) {

        return ResponseEntity.ok(classService.getAdminClassById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new running class")
    public ResponseEntity<ClassResponse> createClass(
            @Valid @RequestBody ClassRequest request) {

        ClassResponse created = classService.createClass(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a running class")
    public ResponseEntity<ClassResponse> updateClass(
            @Parameter(description = "Class ID") @PathVariable String id,
            @Valid @RequestBody ClassRequest request) {

        return ResponseEntity.ok(classService.updateClass(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a running class",
               description = "Permanently removes the class. Existing enrollments remain in DB for audit.")
    public ResponseEntity<Void> deleteClass(
            @Parameter(description = "Class ID") @PathVariable String id) {

        classService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }

    // ── Enrollments ───────────────────────────────────────────────────────────

    @GetMapping("/enrollments")
    @Operation(summary = "List all enrollments",
               description = "Paginated list of enrollments. Filter by classId and/or status.")
    public ResponseEntity<PageResponse<EnrollmentResponse>> getEnrollments(
            @Parameter(description = "Filter by class ID") @RequestParam(required = false) String classId,
            @Parameter(description = "Filter by status: PENDING, CONFIRMED, REJECTED, CANCELLED")
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(classService.getAdminEnrollments(classId, status, page, size));
    }

    @GetMapping("/enrollments/{id}")
    @Operation(summary = "Get a single enrollment by ID")
    public ResponseEntity<EnrollmentResponse> getEnrollmentById(
            @Parameter(description = "Enrollment ID") @PathVariable String id) {

        return ResponseEntity.ok(classService.getAdminEnrollmentById(id));
    }

    @PostMapping("/enrollments/{id}/confirm")
    @Operation(summary = "Confirm (approve) a PENDING enrollment",
               description = "Sets enrollment status to CONFIRMED and increments the class enrolledCount.")
    public ResponseEntity<EnrollmentResponse> confirmEnrollment(
            @Parameter(description = "Enrollment ID") @PathVariable String id,
            @AuthenticationPrincipal Admin admin) {

        String adminId = (admin != null) ? admin.getId() : "admin";
        return ResponseEntity.ok(classService.confirmEnrollment(id, adminId));
    }

    @PostMapping("/enrollments/{id}/reject")
    @Operation(summary = "Reject a PENDING enrollment",
               description = "Sets enrollment status to REJECTED. A reason must be provided.")
    public ResponseEntity<EnrollmentResponse> rejectEnrollment(
            @Parameter(description = "Enrollment ID") @PathVariable String id,
            @Valid @RequestBody RejectEnrollmentRequest request) {

        return ResponseEntity.ok(classService.rejectEnrollment(id, request.getReason()));
    }

    @DeleteMapping("/enrollments/{id}")
    @Operation(summary = "Delete an enrollment record",
               description = "Permanently deletes the enrollment. If CONFIRMED, class enrolledCount is decremented.")
    public ResponseEntity<Void> deleteEnrollment(
            @Parameter(description = "Enrollment ID") @PathVariable String id) {

        classService.deleteEnrollment(id);
        return ResponseEntity.noContent().build();
    }
}
