package com.blogapp.runningclass.entity;

import com.blogapp.runningclass.enums.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Captures a student's enrollment request for a specific RunningClass.
 * <p>
 * Lifecycle: PENDING → CONFIRMED (admin) or REJECTED (admin) → CANCELLED (user)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "enrollments")
@CompoundIndexes({
    // Prevent a user from enrolling in the same class more than once (unless previous is REJECTED/CANCELLED)
    @CompoundIndex(name = "user_class_idx", def = "{'userId': 1, 'classId': 1}")
})
public class Enrollment {

    @Id
    private String id;

    // ── References ────────────────────────────────────────────────────────────
    @Indexed
    private String classId;     // References RunningClass._id

    @Indexed
    private String userId;      // JWT principal's User._id

    // ── Student contact details ───────────────────────────────────────────────
    private String studentName;

    private String parentName;

    @Indexed
    private String email;           // Populated from JWT; shown in admin view

    private String mobileNumber;    // Student/parent contact

    private String gradeOrClass;    // e.g. "12th Science", "B.Sc 2nd Year"

    private String schoolOrCollege; // Name of institution

    private String preferredBatch;  // User's preferred timing if multiple options

    /** Any specific requirement or message from the student */
    private String message;

    // ── Workflow ─────────────────────────────────────────────────────────────
    @Builder.Default
    @Indexed
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    private LocalDateTime submittedAt;

    private LocalDateTime confirmedAt;

    private String confirmedByAdminId;

    private String rejectionReason;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
