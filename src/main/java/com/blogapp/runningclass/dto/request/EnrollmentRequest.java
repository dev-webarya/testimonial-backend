package com.blogapp.runningclass.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request body for a student to enroll in a running class")
public class EnrollmentRequest {

    // ── Student details ──────────────────────────────────────────────────────

    @NotBlank(message = "Student name is required")
    @Schema(example = "Rahul Sharma")
    private String studentName;

    @NotBlank(message = "Parent name is required")
    @Schema(example = "Ramesh Sharma")
    private String parentName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Invalid mobile number")
    @Schema(example = "+919876543210")
    private String mobileNumber;

    @NotBlank(message = "Grade or class is required")
    @Schema(example = "B.Sc 2nd Year")
    private String gradeOrClass;

    @NotBlank(message = "School or college is required")
    @Schema(example = "Delhi University")
    private String schoolOrCollege;

    @Schema(example = "Evening batch 6 PM")
    private String preferredBatch;

    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    @Schema(example = "I am looking to improve my calculus skills for competitive exams.")
    private String message;
}
