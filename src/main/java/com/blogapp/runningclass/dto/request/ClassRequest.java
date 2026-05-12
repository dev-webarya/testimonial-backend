package com.blogapp.runningclass.dto.request;

import com.blogapp.runningclass.enums.ClassCategory;
import com.blogapp.runningclass.enums.ClassStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Request body to create or update a running class")
public class ClassRequest {

    @NotBlank(message = "Title is required")
    @Schema(example = "UG Mathematics")
    private String title;

    @Schema(example = "Comprehensive mathematics coverage for B.Sc and B.Tech students")
    private String description;

    @NotNull(message = "Category is required")
    @Schema(example = "UNDERGRADUATE")
    private ClassCategory category;

    @NotBlank(message = "Schedule is required")
    @Schema(example = "Mon, Wed, Fri - 6:00 PM IST")
    private String schedule;

    @Schema(example = "12-15")
    private String batchSize;

    @NotBlank(message = "Instructor name is required")
    @Schema(example = "Ms. Neha Aggarwal")
    private String instructorName;

    @Schema(example = "10+ years in competitive exam coaching")
    private String instructorBio;

    @Schema(example = "₹5,000 / month")
    private String feeInfo;

    @Schema(example = "2024-06-01T09:00:00")
    private LocalDateTime startDate;

    @Schema(example = "2024-09-30T18:00:00")
    private LocalDateTime endDate;

    @Schema(example = "Students must bring their own textbooks")
    private String additionalInfo;

    @Schema(example = "ACTIVE")
    private ClassStatus status;

    @Schema(example = "20")
    private Integer maxCapacity;
}
