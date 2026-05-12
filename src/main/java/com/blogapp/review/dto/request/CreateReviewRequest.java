package com.blogapp.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Request body to submit a student review")
public class CreateReviewRequest {

    // ── Submitter details ────────────────────────────────────────────────────
    @NotBlank(message = "Student name is required")
    @Schema(example = "Rahul Sharma")
    private String studentName;

    @NotBlank(message = "Parent name is required")
    @Schema(example = "Ramesh Sharma")
    private String parentName;

    @NotBlank(message = "Grade or class is required")
    @Schema(example = "UG Mathematics")
    private String gradeOrClass;

    // ── Review content ──────────────────────────────────────────────────────
    @NotBlank(message = "Review text is required")
    @Size(min = 20, max = 2000, message = "Review must be between 20 and 2000 characters")
    @Schema(example = "Excellent teaching methodology! My concepts became crystal clear through ICFY classes.")
    private String reviewText;

    // ── Overall rating ──────────────────────────────────────────────────────
    @Min(1) @Max(5)
    @Schema(example = "5")
    private int overallRating;

    // ── Detailed ratings ────────────────────────────────────────────────────
    @Min(1) @Max(5) private int teachingQuality;
    @Min(1) @Max(5) private int personalAttention;
    @Min(1) @Max(5) private int testSystem;
    @Min(1) @Max(5) private int overallExperience;
    @Min(1) @Max(5) private int conceptClarity;
    @Min(1) @Max(5) private int doubtSolving;
    @Min(1) @Max(5) private int studyMaterial;
    @Min(1) @Max(5) private int improvementInConfidence;
    @Min(1) @Max(5) private int structuredPlanning;
    @Min(1) @Max(5) private int examOrientedPractice;
    @Min(1) @Max(5) private int reinforcementClasses;
    @Min(1) @Max(5) private int overallSatisfaction;
    @Min(1) @Max(5) private int batchSizeAdvantage;
    @Min(1) @Max(5) private int individualMonitoring;
    @Min(1) @Max(5) private int teacherExperience;
    @Min(1) @Max(5) private int resultImprovement;
}
