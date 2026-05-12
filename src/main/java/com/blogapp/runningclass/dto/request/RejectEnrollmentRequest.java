package com.blogapp.runningclass.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Rejection reason for an enrollment")
public class RejectEnrollmentRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(min = 5, max = 500, message = "Reason must be between 5 and 500 characters")
    @Schema(example = "Batch is full. Please enroll in the next available batch.")
    private String reason;
}
