package com.blogapp.runningclass.mapper;

import com.blogapp.runningclass.dto.request.ClassRequest;
import com.blogapp.runningclass.dto.request.EnrollmentRequest;
import com.blogapp.runningclass.dto.response.ClassResponse;
import com.blogapp.runningclass.dto.response.EnrollmentResponse;
import com.blogapp.runningclass.entity.Enrollment;
import com.blogapp.runningclass.entity.RunningClass;
import com.blogapp.runningclass.enums.ClassStatus;
import com.blogapp.runningclass.enums.EnrollmentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RunningClassMapper {

    // ── RunningClass ──────────────────────────────────────────────────────────

    public RunningClass toEntity(ClassRequest request) {
        return RunningClass.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .schedule(request.getSchedule())
                .batchSize(request.getBatchSize())
                .instructorName(request.getInstructorName())
                .instructorBio(request.getInstructorBio())
                .feeInfo(request.getFeeInfo())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .additionalInfo(request.getAdditionalInfo())
                .status(request.getStatus() != null ? request.getStatus() : ClassStatus.ACTIVE)
                .maxCapacity(request.getMaxCapacity())
                .build();
    }

    public ClassResponse toResponse(RunningClass entity) {
        Integer available = null;
        if (entity.getMaxCapacity() != null) {
            available = Math.max(0, entity.getMaxCapacity() - entity.getEnrolledCount());
        }
        return ClassResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .schedule(entity.getSchedule())
                .batchSize(entity.getBatchSize())
                .instructorName(entity.getInstructorName())
                .instructorBio(entity.getInstructorBio())
                .feeInfo(entity.getFeeInfo())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .additionalInfo(entity.getAdditionalInfo())
                .status(entity.getStatus())
                .maxCapacity(entity.getMaxCapacity())
                .enrolledCount(entity.getEnrolledCount())
                .availableSeats(available)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    // ── Enrollment ────────────────────────────────────────────────────────────

    public Enrollment toEnrollment(EnrollmentRequest request, String classId, String userId, String email) {
        return Enrollment.builder()
                .classId(classId)
                .userId(userId)
                .email(email)
                .studentName(request.getStudentName())
                .parentName(request.getParentName())
                .mobileNumber(request.getMobileNumber())
                .gradeOrClass(request.getGradeOrClass())
                .schoolOrCollege(request.getSchoolOrCollege())
                .preferredBatch(request.getPreferredBatch())
                .message(request.getMessage())
                .status(EnrollmentStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Full enrollment response including email and admin-only fields.
     * Used by admin endpoints.
     */
    public EnrollmentResponse toFullResponse(Enrollment enrollment, RunningClass runningClass) {
        return buildBase(enrollment, runningClass)
                .email(enrollment.getEmail())
                .confirmedByAdminId(enrollment.getConfirmedByAdminId())
                .rejectionReason(enrollment.getRejectionReason())
                .build();
    }

    /**
     * User-facing response — omits email, confirmedByAdminId.
     */
    public EnrollmentResponse toUserResponse(Enrollment enrollment, RunningClass runningClass) {
        return buildBase(enrollment, runningClass)
                .rejectionReason(enrollment.getRejectionReason()) // user should see why they were rejected
                .build();
    }

    // ── Private helper ────────────────────────────────────────────────────────

    private EnrollmentResponse.EnrollmentResponseBuilder buildBase(Enrollment e, RunningClass rc) {
        return EnrollmentResponse.builder()
                .id(e.getId())
                .classId(e.getClassId())
                .classTitle(rc != null ? rc.getTitle() : null)
                .classSchedule(rc != null ? rc.getSchedule() : null)
                .studentName(e.getStudentName())
                .parentName(e.getParentName())
                .mobileNumber(e.getMobileNumber())
                .gradeOrClass(e.getGradeOrClass())
                .schoolOrCollege(e.getSchoolOrCollege())
                .preferredBatch(e.getPreferredBatch())
                .message(e.getMessage())
                .status(e.getStatus())
                .submittedAt(e.getSubmittedAt())
                .confirmedAt(e.getConfirmedAt())
                .createdAt(e.getCreatedAt());
    }
}
