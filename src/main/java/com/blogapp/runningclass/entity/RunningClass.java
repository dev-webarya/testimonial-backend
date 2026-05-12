package com.blogapp.runningclass.entity;

import com.blogapp.runningclass.enums.ClassCategory;
import com.blogapp.runningclass.enums.ClassStatus;
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

/**
 * Represents a running/active class offered by iThinkLearn.
 * <p>
 * Admins manage the full CRUD lifecycle.
 * Students can view ACTIVE classes publicly and enroll when logged in.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "running_classes")
public class RunningClass {

    @Id
    private String id;

    // ── Core identity ────────────────────────────────────────────────────────
    @Indexed
    private String title;           // e.g. "UG Mathematics"

    private String description;     // e.g. "Comprehensive mathematics coverage for B.Sc and B.Tech students"

    @Indexed
    private ClassCategory category; // UNDERGRADUATE | POST_GRADUATE | PROFESSIONAL

    // ── Session details ──────────────────────────────────────────────────────
    private String schedule;        // e.g. "Mon, Wed, Fri - 6:00 PM IST"

    private String batchSize;       // e.g. "12-15"

    private String instructorName;  // e.g. "Ms. Neha Aggarwal"

    private String instructorBio;   // optional short bio

    /** Optional fee info, e.g. "₹5,000 / month" */
    private String feeInfo;

    /** Start date of the class batch */
    private LocalDateTime startDate;

    /** End date of the class batch */
    private LocalDateTime endDate;

    /** Any additional notes for students */
    private String additionalInfo;

    // ── Status ────────────────────────────────────────────────────────────────
    @Builder.Default
    @Indexed
    private ClassStatus status = ClassStatus.ACTIVE;

    /** Maximum enrollment capacity (null = unlimited) */
    private Integer maxCapacity;

    /** Current confirmed enrollment count — maintained by service on confirm/cancel */
    @Builder.Default
    private int enrolledCount = 0;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
