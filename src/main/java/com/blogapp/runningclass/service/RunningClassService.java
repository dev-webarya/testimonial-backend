package com.blogapp.runningclass.service;

import com.blogapp.common.dto.PageResponse;
import com.blogapp.runningclass.dto.request.ClassRequest;
import com.blogapp.runningclass.dto.request.EnrollmentRequest;
import com.blogapp.runningclass.dto.response.ClassResponse;
import com.blogapp.runningclass.dto.response.EnrollmentResponse;

import java.util.List;

public interface RunningClassService {

    // ── Running Classes ───────────────────────────────────────────────────────

    /** Public: list ACTIVE classes, optionally filtered by category */
    PageResponse<ClassResponse> getActiveClasses(String category, int page, int size);

    /** Public: get a single ACTIVE class by id */
    ClassResponse getActiveClassById(String id);

    /** Admin: full CRUD listing with optional status + category filters */
    PageResponse<ClassResponse> getAdminClasses(String status, String category, int page, int size);

    /** Admin: get any class by id regardless of status */
    ClassResponse getAdminClassById(String id);

    /** Admin: create a new class */
    ClassResponse createClass(ClassRequest request);

    /** Admin: update an existing class */
    ClassResponse updateClass(String id, ClassRequest request);

    /** Admin: delete a class (and optionally cascade-cancel enrollments) */
    void deleteClass(String id);

    // ── Enrollments ───────────────────────────────────────────────────────────

    /** User: enroll in a class */
    EnrollmentResponse enroll(String classId, EnrollmentRequest request, String userId, String email);

    /** User: cancel own enrollment */
    EnrollmentResponse cancelMyEnrollment(String enrollmentId, String userId);

    /** User: get all my enrollments */
    List<EnrollmentResponse> getMyEnrollments(String userId);

    /** Admin: list all enrollments, optionally filtered by classId and/or status */
    PageResponse<EnrollmentResponse> getAdminEnrollments(String classId, String status, int page, int size);

    /** Admin: get a single enrollment */
    EnrollmentResponse getAdminEnrollmentById(String id);

    /** Admin: confirm (approve) a PENDING enrollment */
    EnrollmentResponse confirmEnrollment(String id, String adminId);

    /** Admin: reject a PENDING enrollment */
    EnrollmentResponse rejectEnrollment(String id, String reason);

    /** Admin: delete an enrollment record */
    void deleteEnrollment(String id);
}
