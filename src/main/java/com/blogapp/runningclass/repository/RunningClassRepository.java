package com.blogapp.runningclass.repository;

import com.blogapp.runningclass.entity.RunningClass;
import com.blogapp.runningclass.enums.ClassCategory;
import com.blogapp.runningclass.enums.ClassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RunningClassRepository extends MongoRepository<RunningClass, String> {

    /** Public listing — all ACTIVE classes, optionally filtered by category */
    Page<RunningClass> findByStatus(ClassStatus status, Pageable pageable);

    Page<RunningClass> findByStatusAndCategory(ClassStatus status, ClassCategory category, Pageable pageable);

    /** Admin listing — all classes optionally filtered by status and/or category */
    Page<RunningClass> findByCategory(ClassCategory category, Pageable pageable);

    Page<RunningClass> findByStatusAndCategoryOrderByCreatedAtDesc(ClassStatus status, ClassCategory category, Pageable pageable);
}
