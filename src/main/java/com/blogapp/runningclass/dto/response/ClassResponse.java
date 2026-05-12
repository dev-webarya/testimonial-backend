package com.blogapp.runningclass.dto.response;

import com.blogapp.runningclass.enums.ClassCategory;
import com.blogapp.runningclass.enums.ClassStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Running class response")
public class ClassResponse {

    private String id;
    private String title;
    private String description;
    private ClassCategory category;
    private String schedule;
    private String batchSize;
    private String instructorName;
    private String instructorBio;
    private String feeInfo;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String additionalInfo;
    private ClassStatus status;
    private Integer maxCapacity;
    private int enrolledCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Derived: how many seats remain (null if maxCapacity is not set) */
    private Integer availableSeats;
}
