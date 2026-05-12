package com.blogapp.review.dto.response;

import com.blogapp.review.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private String id;

    // ── Submitter ────────────────────────────────────────────────────────────
    private String studentName;
    private String parentName;
    private String gradeOrClass;

    /** Email is hidden from public responses – included for admin use only */
    private String email;

    // ── Review content ──────────────────────────────────────────────────────
    private String reviewText;

    // ── Overall ─────────────────────────────────────────────────────────────
    private int overallRating;

    // ── Detailed ratings ────────────────────────────────────────────────────
    private int teachingQuality;
    private int personalAttention;
    private int testSystem;
    private int overallExperience;
    private int conceptClarity;
    private int doubtSolving;
    private int studyMaterial;
    private int improvementInConfidence;
    private int structuredPlanning;
    private int examOrientedPractice;
    private int reinforcementClasses;
    private int overallSatisfaction;
    private int batchSizeAdvantage;
    private int individualMonitoring;
    private int teacherExperience;
    private int resultImprovement;

    // ── Workflow state ───────────────────────────────────────────────────────
    private ReviewStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime publishedAt;

    /** Only present for admin responses */
    private String rejectionReason;
    private String approvedByAdminId;

    private LocalDateTime createdAt;
}
