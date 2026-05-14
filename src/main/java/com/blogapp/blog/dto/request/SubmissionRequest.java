package com.blogapp.blog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonAlias;

public class SubmissionRequest {

    @Data
    @Schema(description = "Request to start blog submission via OTP")
    public static class Start {
        @NotBlank(message = "Author email is required")
        @Email(message = "Invalid email format")
        @Schema(example = "jane@example.com")
        @JsonAlias("email")
        private String authorEmail;

        @Schema(description = "Set to true if this is explicitly a resend request", example = "false")
        private boolean isResend;
    }

    @Data
    @Schema(description = "Request to verify OTP for blog submission")
    public static class Verify {
        @NotBlank(message = "Author email is required")
        @Email(message = "Invalid email format")
        @Schema(example = "jane@example.com")
        @JsonAlias("email")
        private String authorEmail;

        @NotBlank(message = "OTP code is required")
        @Schema(example = "123456")
        private String otp;
    }
}
