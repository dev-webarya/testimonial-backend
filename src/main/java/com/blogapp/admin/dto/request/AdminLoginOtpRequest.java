package com.blogapp.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request to send OTP for Admin login")
public class AdminLoginOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(example = "admin@astarclasses.com")
    private String email;

    @Schema(description = "Set to true if this is explicitly a resend request", example = "false")
    private boolean isResend;
}
