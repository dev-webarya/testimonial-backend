package com.blogapp.runningclass.repository;

import com.blogapp.runningclass.entity.Enrollment;
import com.blogapp.runningclass.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {

    /** Check for an existing non-cancelled/non-rejected enrollment by this user for this class */
    boolean existsByUserIdAndClassIdAndStatusNotIn(String userId, String classId, List<EnrollmentStatus> statuses);

    /** User's own enrollment history across all classes */
    List<Enrollment> findByUserIdOrderByCreatedAtDesc(String userId);

    /** User's enrollment for a specific class */
    Optional<Enrollment> findByUserIdAndClassId(String userId, String classId);

    /** Admin: all enrollments for a given class, paginated */
    Page<Enrollment> findByClassId(String classId, Pageable pageable);

    /** Admin: all enrollments filtered by status, paginated */
    Page<Enrollment> findByStatus(EnrollmentStatus status, Pageable pageable);

    /** Admin: enrollments for a specific class filtered by status */
    Page<Enrollment> findByClassIdAndStatus(String classId, EnrollmentStatus status, Pageable pageable);

    /** Count confirmed enrollments for a class — used for capacity guard */
    long countByClassIdAndStatus(String classId, EnrollmentStatus status);
}
