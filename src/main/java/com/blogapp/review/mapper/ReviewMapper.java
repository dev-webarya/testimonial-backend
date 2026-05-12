package com.blogapp.review.mapper;

import com.blogapp.review.dto.request.CreateReviewRequest;
import com.blogapp.review.dto.response.ReviewResponse;
import com.blogapp.review.entity.StudentReview;
import com.blogapp.review.enums.ReviewStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ReviewMapper {

    /**
     * Maps the request + authenticated user info into a new StudentReview entity
     * in PENDING status.
     */
    public StudentReview toEntity(CreateReviewRequest request, String userId, String email) {
        return StudentReview.builder()
                .userId(userId)
                .email(email)
                .studentName(request.getStudentName())
                .parentName(request.getParentName())
                .gradeOrClass(request.getGradeOrClass())
                .reviewText(request.getReviewText())
                .overallRating(request.getOverallRating())
                // Detailed ratings
                .teachingQuality(request.getTeachingQuality())
                .personalAttention(request.getPersonalAttention())
                .testSystem(request.getTestSystem())
                .overallExperience(request.getOverallExperience())
                .conceptClarity(request.getConceptClarity())
                .doubtSolving(request.getDoubtSolving())
                .studyMaterial(request.getStudyMaterial())
                .improvementInConfidence(request.getImprovementInConfidence())
                .structuredPlanning(request.getStructuredPlanning())
                .examOrientedPractice(request.getExamOrientedPractice())
                .reinforcementClasses(request.getReinforcementClasses())
                .overallSatisfaction(request.getOverallSatisfaction())
                .batchSizeAdvantage(request.getBatchSizeAdvantage())
                .individualMonitoring(request.getIndividualMonitoring())
                .teacherExperience(request.getTeacherExperience())
                .resultImprovement(request.getResultImprovement())
                // Workflow
                .status(ReviewStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Full response including admin-only fields (email, rejectionReason, approvedByAdminId).
     */
    public ReviewResponse toFullResponse(StudentReview entity) {
        return buildBase(entity)
                .email(entity.getEmail())
                .rejectionReason(entity.getRejectionReason())
                .approvedByAdminId(entity.getApprovedByAdminId())
                .build();
    }

    /**
     * Public response — email and admin-only fields are omitted.
     */
    public ReviewResponse toPublicResponse(StudentReview entity) {
        return buildBase(entity).build();
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private ReviewResponse.ReviewResponseBuilder buildBase(StudentReview entity) {
        return ReviewResponse.builder()
                .id(entity.getId())
                .studentName(entity.getStudentName())
                .parentName(entity.getParentName())
                .gradeOrClass(entity.getGradeOrClass())
                .reviewText(entity.getReviewText())
                .overallRating(entity.getOverallRating())
                .teachingQuality(entity.getTeachingQuality())
                .personalAttention(entity.getPersonalAttention())
                .testSystem(entity.getTestSystem())
                .overallExperience(entity.getOverallExperience())
                .conceptClarity(entity.getConceptClarity())
                .doubtSolving(entity.getDoubtSolving())
                .studyMaterial(entity.getStudyMaterial())
                .improvementInConfidence(entity.getImprovementInConfidence())
                .structuredPlanning(entity.getStructuredPlanning())
                .examOrientedPractice(entity.getExamOrientedPractice())
                .reinforcementClasses(entity.getReinforcementClasses())
                .overallSatisfaction(entity.getOverallSatisfaction())
                .batchSizeAdvantage(entity.getBatchSizeAdvantage())
                .individualMonitoring(entity.getIndividualMonitoring())
                .teacherExperience(entity.getTeacherExperience())
                .resultImprovement(entity.getResultImprovement())
                .status(entity.getStatus())
                .submittedAt(entity.getSubmittedAt())
                .publishedAt(entity.getPublishedAt())
                .createdAt(entity.getCreatedAt());
    }
}
