package com.blogapp.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendOtpRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email address to send the OTP to", example = "student@email.com")
    private String email;

    @Schema(description = "Set to true if this is explicitly a resend request", example = "false")
    private boolean isResend;
}
