package com.blogapp.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request to start login by sending an OTP to the user's email")
public class AuthStartRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    @Schema(example = "reader@example.com")
    private String email;

    @Schema(description = "Set to true if this is explicitly a resend request to force a new OTP generation", example = "false")
    private boolean isResend;
}
