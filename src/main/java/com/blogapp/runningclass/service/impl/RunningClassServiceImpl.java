package com.blogapp.runningclass.service.impl;

import com.blogapp.common.dto.PageResponse;
import com.blogapp.common.exception.BadRequestException;
import com.blogapp.common.exception.ResourceNotFoundException;
import com.blogapp.runningclass.dto.request.ClassRequest;
import com.blogapp.runningclass.dto.request.EnrollmentRequest;
import com.blogapp.runningclass.dto.response.ClassResponse;
import com.blogapp.runningclass.dto.response.EnrollmentResponse;
import com.blogapp.runningclass.entity.Enrollment;
import com.blogapp.runningclass.entity.RunningClass;
import com.blogapp.runningclass.enums.ClassCategory;
import com.blogapp.runningclass.enums.ClassStatus;
import com.blogapp.runningclass.enums.EnrollmentStatus;
import com.blogapp.runningclass.mapper.RunningClassMapper;
import com.blogapp.runningclass.repository.EnrollmentRepository;
import com.blogapp.runningclass.repository.RunningClassRepository;
import com.blogapp.runningclass.service.RunningClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunningClassServiceImpl implements RunningClassService {

    private final RunningClassRepository classRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RunningClassMapper mapper;

    // ────────────────────────────── CLASSES ──────────────────────────────────

    @Override
    public PageResponse<ClassResponse> getActiveClasses(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "title"));
        Page<RunningClass> result;

        if (category != null && !category.isBlank()) {
            ClassCategory cat = parseCategory(category);
            result = classRepository.findByStatusAndCategory(ClassStatus.ACTIVE, cat, pageable);
        } else {
            result = classRepository.findByStatus(ClassStatus.ACTIVE, pageable);
        }

        return buildPage(result.map(mapper::toResponse));
    }

    @Override
    public ClassResponse getActiveClassById(String id) {
        RunningClass rc = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", id));
        if (rc.getStatus() != ClassStatus.ACTIVE) {
            throw new ResourceNotFoundException("Class", "id", id);
        }
        return mapper.toResponse(rc);
    }

    @Override
    public PageResponse<ClassResponse> getAdminClasses(String status, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<RunningClass> result;
        boolean hasStatus = status != null && !status.isBlank();
        boolean hasCat = category != null && !category.isBlank();

        if (hasStatus && hasCat) {
            result = classRepository.findByStatusAndCategory(parseStatus(status), parseCategory(category), pageable);
        } else if (hasStatus) {
            result = classRepository.findByStatus(parseStatus(status), pageable);
        } else if (hasCat) {
            result = classRepository.findByCategory(parseCategory(category), pageable);
        } else {
            result = classRepository.findAll(pageable);
        }

        return buildPage(result.map(mapper::toResponse));
    }

    @Override
    public ClassResponse getAdminClassById(String id) {
        RunningClass rc = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", id));
        return mapper.toResponse(rc);
    }

    @Override
    public ClassResponse createClass(ClassRequest request) {
        RunningClass rc = mapper.toEntity(request);
        RunningClass saved = classRepository.save(rc);
        log.info("Running class created: id={} title={}", saved.getId(), saved.getTitle());
        return mapper.toResponse(saved);
    }

    @Override
    public ClassResponse updateClass(String id, ClassRequest request) {
        RunningClass rc = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", id));

        rc.setTitle(request.getTitle());
        rc.setDescription(request.getDescription());
        rc.setCategory(request.getCategory());
        rc.setSchedule(request.getSchedule());
        rc.setBatchSize(request.getBatchSize());
        rc.setInstructorName(request.getInstructorName());
        rc.setInstructorBio(request.getInstructorBio());
        rc.setFeeInfo(request.getFeeInfo());
        rc.setStartDate(request.getStartDate());
        rc.setEndDate(request.getEndDate());
        rc.setAdditionalInfo(request.getAdditionalInfo());
        rc.setMaxCapacity(request.getMaxCapacity());
        if (request.getStatus() != null) {
            rc.setStatus(request.getStatus());
        }

        RunningClass saved = classRepository.save(rc);
        log.info("Running class updated: id={}", id);
        return mapper.toResponse(saved);
    }

    @Override
    public void deleteClass(String id) {
        RunningClass rc = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", id));
        classRepository.delete(rc);
        log.info("Running class deleted: id={}", id);
    }

    // ────────────────────────────── ENROLLMENTS ───────────────────────────────

    @Override
    public EnrollmentResponse enroll(String classId, EnrollmentRequest request, String userId, String email) {
        // 1. Class must exist and be ACTIVE
        RunningClass rc = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", classId));

        if (rc.getStatus() != ClassStatus.ACTIVE) {
            throw new BadRequestException("Enrollments are only open for ACTIVE classes.");
        }

        // 2. Prevent duplicate active enrollment
        List<EnrollmentStatus> excludedStatuses = Arrays.asList(
                EnrollmentStatus.REJECTED, EnrollmentStatus.CANCELLED);
        boolean alreadyEnrolled = enrollmentRepository
                .existsByUserIdAndClassIdAndStatusNotIn(userId, classId, excludedStatuses);
        if (alreadyEnrolled) {
            throw new BadRequestException(
                    "You already have an active enrollment for this class. " +
                    "You may re-enroll only after your previous enrollment is rejected or cancelled.");
        }

        // 3. Capacity check (only if maxCapacity is set)
        if (rc.getMaxCapacity() != null) {
            long confirmed = enrollmentRepository
                    .countByClassIdAndStatus(classId, EnrollmentStatus.CONFIRMED);
            if (confirmed >= rc.getMaxCapacity()) {
                throw new BadRequestException("This class is full. No more enrollments are being accepted.");
            }
        }

        // 4. Save enrollment
        Enrollment enrollment = mapper.toEnrollment(request, classId, userId, email);
        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("Enrollment submitted: classId={} userId={} enrollmentId={}", classId, userId, saved.getId());

        return mapper.toUserResponse(saved, rc);
    }

    @Override
    public EnrollmentResponse cancelMyEnrollment(String enrollmentId, String userId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        if (!enrollment.getUserId().equals(userId)) {
            throw new BadRequestException("You can only cancel your own enrollment.");
        }
        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new BadRequestException("Enrollment is already cancelled.");
        }
        if (enrollment.getStatus() == EnrollmentStatus.REJECTED) {
            throw new BadRequestException("Cannot cancel a rejected enrollment.");
        }

        EnrollmentStatus previousStatus = enrollment.getStatus();
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        Enrollment saved = enrollmentRepository.save(enrollment);

        // Decrement enrolledCount if the cancelled one was CONFIRMED
        if (previousStatus == EnrollmentStatus.CONFIRMED) {
            classRepository.findById(enrollment.getClassId()).ifPresent(rc -> {
                rc.setEnrolledCount(Math.max(0, rc.getEnrolledCount() - 1));
                classRepository.save(rc);
            });
        }

        RunningClass rc = classRepository.findById(enrollment.getClassId()).orElse(null);
        log.info("Enrollment cancelled by user: enrollmentId={} userId={}", enrollmentId, userId);
        return mapper.toUserResponse(saved, rc);
    }

    @Override
    public List<EnrollmentResponse> getMyEnrollments(String userId) {
        return enrollmentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(e -> {
                    RunningClass rc = classRepository.findById(e.getClassId()).orElse(null);
                    return mapper.toUserResponse(e, rc);
                })
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<EnrollmentResponse> getAdminEnrollments(String classId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        Page<Enrollment> result;

        boolean hasClass = classId != null && !classId.isBlank();
        boolean hasStatus = status != null && !status.isBlank();

        if (hasClass && hasStatus) {
            result = enrollmentRepository.findByClassIdAndStatus(classId, parseEnrollmentStatus(status), pageable);
        } else if (hasClass) {
            result = enrollmentRepository.findByClassId(classId, pageable);
        } else if (hasStatus) {
            result = enrollmentRepository.findByStatus(parseEnrollmentStatus(status), pageable);
        } else {
            result = enrollmentRepository.findAll(pageable);
        }

        return buildPage(result.map(e -> {
            RunningClass rc = classRepository.findById(e.getClassId()).orElse(null);
            return mapper.toFullResponse(e, rc);
        }));
    }

    @Override
    public EnrollmentResponse getAdminEnrollmentById(String id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
        RunningClass rc = classRepository.findById(enrollment.getClassId()).orElse(null);
        return mapper.toFullResponse(enrollment, rc);
    }

    @Override
    public EnrollmentResponse confirmEnrollment(String id, String adminId) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));

        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING enrollments can be confirmed. Current status: " + enrollment.getStatus());
        }

        // Re-check capacity before confirming
        RunningClass rc = classRepository.findById(enrollment.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class", "id", enrollment.getClassId()));

        if (rc.getMaxCapacity() != null) {
            long confirmed = enrollmentRepository
                    .countByClassIdAndStatus(enrollment.getClassId(), EnrollmentStatus.CONFIRMED);
            if (confirmed >= rc.getMaxCapacity()) {
                throw new BadRequestException(
                        "Cannot confirm — class is already at full capacity (" + rc.getMaxCapacity() + ").");
            }
        }

        enrollment.setStatus(EnrollmentStatus.CONFIRMED);
        enrollment.setConfirmedAt(LocalDateTime.now());
        enrollment.setConfirmedByAdminId(adminId);
        enrollment.setRejectionReason(null);

        Enrollment saved = enrollmentRepository.save(enrollment);

        // Increment class enrolledCount
        rc.setEnrolledCount(rc.getEnrolledCount() + 1);
        classRepository.save(rc);

        log.info("Enrollment confirmed: id={} by adminId={}", id, adminId);
        return mapper.toFullResponse(saved, rc);
    }

    @Override
    public EnrollmentResponse rejectEnrollment(String id, String reason) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));

        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING enrollments can be rejected. Current status: " + enrollment.getStatus());
        }

        enrollment.setStatus(EnrollmentStatus.REJECTED);
        enrollment.setRejectionReason(reason);

        Enrollment saved = enrollmentRepository.save(enrollment);
        RunningClass rc = classRepository.findById(enrollment.getClassId()).orElse(null);

        log.info("Enrollment rejected: id={} reason={}", id, reason);
        return mapper.toFullResponse(saved, rc);
    }

    @Override
    public void deleteEnrollment(String id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));

        // If confirmed, decrement enrolledCount
        if (enrollment.getStatus() == EnrollmentStatus.CONFIRMED) {
            classRepository.findById(enrollment.getClassId()).ifPresent(rc -> {
                rc.setEnrolledCount(Math.max(0, rc.getEnrolledCount() - 1));
                classRepository.save(rc);
            });
        }

        enrollmentRepository.delete(enrollment);
        log.info("Enrollment deleted: id={}", id);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ClassStatus parseStatus(String s) {
        try {
            return ClassStatus.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid class status: " + s
                    + ". Valid values: ACTIVE, INACTIVE, COMPLETED, CANCELLED");
        }
    }

    private ClassCategory parseCategory(String c) {
        try {
            return ClassCategory.valueOf(c.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid category: " + c
                    + ". Valid values: UNDERGRADUATE, POST_GRADUATE, PROFESSIONAL");
        }
    }

    private EnrollmentStatus parseEnrollmentStatus(String s) {
        try {
            return EnrollmentStatus.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid enrollment status: " + s
                    + ". Valid values: PENDING, CONFIRMED, REJECTED, CANCELLED");
        }
    }

    private <T> PageResponse<T> buildPage(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
