package com.blogapp.runningclass.dto.response;

import com.blogapp.runningclass.enums.EnrollmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Enrollment response")
public class EnrollmentResponse {

    private String id;

    // Class summary (denormalized for convenience)
    private String classId;
    private String classTitle;
    private String classSchedule;

    // Student details
    private String studentName;
    private String parentName;

    /** Hidden from public/user endpoint — visible only to admin */
    private String email;

    private String mobileNumber;
    private String gradeOrClass;
    private String schoolOrCollege;
    private String preferredBatch;
    private String message;

    // Workflow
    private EnrollmentStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime confirmedAt;
    private String rejectionReason;
    private String confirmedByAdminId;

    private LocalDateTime createdAt;
}
