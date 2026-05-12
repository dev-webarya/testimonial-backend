package com.blogapp.review.repository;

import com.blogapp.review.entity.StudentReview;
import com.blogapp.review.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentReviewRepository extends MongoRepository<StudentReview, String> {

    /** Fetch all reviews with a given status – used by admin panel */
    Page<StudentReview> findByStatus(ReviewStatus status, Pageable pageable);

    /** Fetch all published reviews – used by public listing */
    Page<StudentReview> findByStatusOrderByPublishedAtDesc(ReviewStatus status, Pageable pageable);

    /** Check if a user has already submitted a review (one per user) */
    boolean existsByUserIdAndStatusNot(String userId, ReviewStatus status);

    /** My reviews – logged-in user's own submission history */
    List<StudentReview> findByUserIdOrderByCreatedAtDesc(String userId);
}
