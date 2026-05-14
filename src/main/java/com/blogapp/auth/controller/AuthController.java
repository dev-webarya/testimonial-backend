package com.blogapp.auth.controller;

import com.blogapp.auth.dto.request.AuthStartRequest;
import com.blogapp.auth.dto.request.AuthVerifyRequest;
import com.blogapp.auth.dto.response.AuthResponse;
import com.blogapp.config.JwtTokenProvider;
import com.blogapp.otp.enums.OtpPurpose;
import com.blogapp.otp.service.OtpService;
import com.blogapp.user.entity.User;
import com.blogapp.user.service.UserService;
import com.blogapp.auth.dto.request.UserPasswordLoginRequest;
import com.blogapp.auth.dto.request.UserForgotPasswordRequest;
import com.blogapp.auth.dto.request.UserResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User login via email OTP")
public class AuthController {

    private final OtpService otpService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login-password")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<?> loginWithPassword(@Valid @RequestBody UserPasswordLoginRequest request) {
        log.info("Received password login request for email: {}", request.getEmail());

        return userService.findByEmail(request.getEmail())
                .filter(user -> user.getPassword() != null && passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(user -> {
                    log.info("Password login successful for email: {}", user.getEmail());
                    String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), "ROLE_USER");
                    
                    AuthResponse response = AuthResponse.builder()
                            .token(token)
                            .tokenType("Bearer")
                            .user(AuthResponse.UserInfo.builder()
                                    .id(user.getId())
                                    .email(user.getEmail())
                                    .name(user.getName())
                                    .emailVerified(user.getEmailVerifiedAt() != null)
                                    .build())
                            .build();
                    return ResponseEntity.ok((Object) response);
                })
                .orElseGet(() -> {
                    log.warn("Failed password login attempt for email: {}", request.getEmail());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("message", "Invalid email or password"));
                });
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request OTP for user password reset")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody UserForgotPasswordRequest request) {
        log.info("Received forgot-password request for email: {}", request.getEmail());

        if (userService.findByEmail(request.getEmail()).isEmpty()) {
            // Prevent email enumeration
            return ResponseEntity.ok(Map.of("message", "If that account exists, an OTP has been sent."));
        }

        boolean sent = otpService.sendOtp(request.getEmail(), OtpPurpose.USER_PASSWORD_RESET, request.isResend());
        if (!sent) {
            return ResponseEntity.ok(Map.of("message", "If that account exists, an OTP was already sent recently. Please check your email."));
        }

        return ResponseEntity.ok(Map.of("message", "If that account exists, an OTP has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset user password using OTP")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody UserResetPasswordRequest request) {
        log.info("Received reset-password request for email: {}", request.getEmail());

        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp(), OtpPurpose.USER_PASSWORD_RESET);
        if (!isValid) {
            log.warn("Invalid OTP attempt for password reset: {}", request.getEmail());
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP"));
        }

        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        userService.updatePassword(user.getId(), passwordEncoder.encode(request.getNewPassword()));

        return ResponseEntity.ok(Map.of("message", "Password has been successfully reset. You can now login."));
    }

    @PostMapping("/start")
    @Operation(summary = "Start login — sends OTP to the given email")
    public ResponseEntity<Map<String, String>> startLogin(
            @Valid @RequestBody AuthStartRequest request) {
        log.info("Received login start request for email: {}", request.getEmail());

        // Send OTP (user record is NOT created here to avoid ghost users, it is created in verifyOtp)
        boolean sent = otpService.sendOtp(request.getEmail(), OtpPurpose.USER_LOGIN, request.isResend());

        if (!sent) {
            log.info("OTP cooldown active, not sending a new OTP to: {}", request.getEmail());
            return ResponseEntity.ok(Map.of(
                    "message", "An OTP was already sent recently. Please check your email."));
        }

        log.info("Successfully initiated OTP for email: {}", request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "OTP sent to " + request.getEmail()));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify OTP — returns JWT token and user info")
    public ResponseEntity<AuthResponse> verifyOtp(
            @Valid @RequestBody AuthVerifyRequest request) {
        log.info("Received OTP verification request for email: {}", request.getEmail());

        boolean valid = otpService.verifyOtp(
                request.getEmail(), request.getOtp(), OtpPurpose.USER_LOGIN);

        if (!valid) {
            log.warn("Invalid OTP attempt for email: {}", request.getEmail());
            return ResponseEntity.badRequest().build();
        }

        log.info("OTP verified successfully for email: {}. Generating session token.", request.getEmail());

        // Mark email as verified and update profile if provided
        User user = userService.findOrCreateByEmail(request.getEmail());
        userService.markEmailVerified(user.getId());

        if (request.getName() != null || request.getMobile() != null) {
            user = userService.updateProfile(user.getId(), request.getName(), request.getMobile());
        }

        // Generate JWT
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), "ROLE_USER");

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .emailVerified(true)
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }
}
