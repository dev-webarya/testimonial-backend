package com.blogapp.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request body to reject a student review with a reason")
public class RejectReviewRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(min = 5, max = 500, message = "Rejection reason must be between 5 and 500 characters")
    @Schema(example = "Review contains inappropriate content")
    private String reason;
}
