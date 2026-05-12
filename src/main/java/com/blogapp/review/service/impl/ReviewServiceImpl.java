package com.blogapp.review.service.impl;

import com.blogapp.common.dto.PageResponse;
import com.blogapp.common.exception.BadRequestException;
import com.blogapp.common.exception.ResourceNotFoundException;
import com.blogapp.review.dto.request.CreateReviewRequest;
import com.blogapp.review.dto.response.ReviewResponse;
import com.blogapp.review.entity.StudentReview;
import com.blogapp.review.enums.ReviewStatus;
import com.blogapp.review.mapper.ReviewMapper;
import com.blogapp.review.repository.StudentReviewRepository;
import com.blogapp.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final StudentReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    // ── User-facing operations ────────────────────────────────────────────────

    @Override
    public ReviewResponse submitReview(CreateReviewRequest request, String userId, String email) {
        // Prevent more than one active (non-rejected) review per user
        boolean hasActive = reviewRepository.existsByUserIdAndStatusNot(userId, ReviewStatus.REJECTED);
        if (hasActive) {
            throw new BadRequestException(
                    "You already have a review pending or published. " +
                    "You may submit a new one only after your previous review is rejected.");
        }

        StudentReview review = reviewMapper.toEntity(request, userId, email);
        StudentReview saved = reviewRepository.save(review);
        log.info("Review submitted by userId={} — id={}", userId, saved.getId());
        return reviewMapper.toFullResponse(saved);
    }

    @Override
    public PageResponse<ReviewResponse> getPublishedReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentReview> reviewPage =
                reviewRepository.findByStatusOrderByPublishedAtDesc(ReviewStatus.PUBLISHED, pageable);

        List<ReviewResponse> content = reviewPage.getContent().stream()
                .map(reviewMapper::toPublicResponse)
                .collect(Collectors.toList());

        return buildPageResponse(content, reviewPage);
    }

    @Override
    public ReviewResponse getPublishedReviewById(String id) {
        StudentReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        if (review.getStatus() != ReviewStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Review", "id", id);
        }
        return reviewMapper.toPublicResponse(review);
    }

    @Override
    public List<ReviewResponse> getMyReviews(String userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(reviewMapper::toFullResponse)
                .collect(Collectors.toList());
    }

    // ── Admin operations ──────────────────────────────────────────────────────

    @Override
    public PageResponse<ReviewResponse> getAdminReviews(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        Page<StudentReview> reviewPage;

        if (status != null && !status.isBlank()) {
            try {
                ReviewStatus reviewStatus = ReviewStatus.valueOf(status.toUpperCase());
                reviewPage = reviewRepository.findByStatus(reviewStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status: " + status
                        + ". Valid values: PENDING, PUBLISHED, REJECTED");
            }
        } else {
            reviewPage = reviewRepository.findAll(pageable);
        }

        List<ReviewResponse> content = reviewPage.getContent().stream()
                .map(reviewMapper::toFullResponse)
                .collect(Collectors.toList());

        return buildPageResponse(content, reviewPage);
    }

    @Override
    public ReviewResponse getAdminReviewById(String id) {
        StudentReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        return reviewMapper.toFullResponse(review);
    }

    @Override
    public ReviewResponse approveReview(String id, String adminId) {
        StudentReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING reviews can be approved. Current status: " + review.getStatus());
        }

        review.setStatus(ReviewStatus.PUBLISHED);
        review.setPublishedAt(LocalDateTime.now());
        review.setApprovedByAdminId(adminId);
        review.setRejectionReason(null);

        StudentReview saved = reviewRepository.save(review);
        log.info("Review approved: id={} by adminId={}", id, adminId);
        return reviewMapper.toFullResponse(saved);
    }

    @Override
    public ReviewResponse rejectReview(String id, String reason) {
        StudentReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING reviews can be rejected. Current status: " + review.getStatus());
        }

        review.setStatus(ReviewStatus.REJECTED);
        review.setRejectionReason(reason);

        StudentReview saved = reviewRepository.save(review);
        log.info("Review rejected: id={} — reason: {}", id, reason);
        return reviewMapper.toFullResponse(saved);
    }

    @Override
    public void deleteReview(String id) {
        StudentReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        reviewRepository.delete(review);
        log.info("Review deleted: id={}", id);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private <T> PageResponse<T> buildPageResponse(List<T> content, Page<?> page) {
        return PageResponse.<T>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
