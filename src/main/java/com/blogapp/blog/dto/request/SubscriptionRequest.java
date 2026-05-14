package com.blogapp.blog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class SubscriptionRequest {

    @Data
    @Schema(description = "Request to start blog subscription via OTP")
    public static class Start {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(example = "jane@example.com")
        private String email;

        @Schema(description = "Set to true if this is explicitly a resend request", example = "false")
        private boolean isResend;
    }

    @Data
    @Schema(description = "Request to verify OTP for blog subscription")
    public static class Verify {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(example = "jane@example.com")
        private String email;

        @NotBlank(message = "OTP is required to subscribe")
        @Schema(example = "123456")
        private String otp;
    }

    @Data
    @Schema(description = "Request to unsubscribe from blog updates")
    public static class Unsubscribe {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Schema(example = "jane@example.com")
        private String email;
    }
}
