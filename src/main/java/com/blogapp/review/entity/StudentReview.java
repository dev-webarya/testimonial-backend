package com.blogapp.review.entity;

import com.blogapp.review.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "student_reviews")
public class StudentReview {

    @Id
    private String id;

    // ── Submitter identity ──────────────────────────────────────────────────
    /** MongoDB User id of the submitter – populated from JWT */
    @Indexed
    private String userId;

    private String studentName;
    private String parentName;

    @Indexed
    private String email;

    private String gradeOrClass;   // e.g. "UG Mathematics", "Grade 10"

    // ── Review content ──────────────────────────────────────────────────────
    private String reviewText;

    // ── Overall rating ──────────────────────────────────────────────────────
    private int overallRating;  // 1-5

    // ── Detailed ratings (all 1-5) ──────────────────────────────────────────
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

    // ── Approval workflow ────────────────────────────────────────────────────
    @Indexed
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    private LocalDateTime submittedAt;

    @Indexed
    private LocalDateTime publishedAt;

    private String approvedByAdminId;
    private String rejectionReason;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
