package com.blogapp.admin.controller;

import com.blogapp.admin.dto.request.AdminAuthRequest;
import com.blogapp.admin.dto.request.AdminForgotRequest;
import com.blogapp.admin.dto.request.AdminResetPasswordRequest;
import com.blogapp.admin.dto.response.AdminAuthResponse;
import com.blogapp.admin.entity.Admin;
import com.blogapp.admin.repository.AdminRepository;
import com.blogapp.config.JwtTokenProvider;
import com.blogapp.otp.enums.OtpPurpose;
import com.blogapp.otp.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping({"/api/admin/auth", "/admin/auth"})
@RequiredArgsConstructor
@Tag(name = "Admin Authentication", description = "Admin login and password reset via OTP")
public class AdminAuthController {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpService otpService;

    @PostMapping("/login")
    @Operation(summary = "Admin login to receive JWT")
    public ResponseEntity<?> login(@Valid @RequestBody AdminAuthRequest request) {
        log.info("Received admin login request for email: {}", request.getEmail());

        Optional<Admin> adminOpt = adminRepository.findByEmail(request.getEmail());

        if (adminOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), adminOpt.get().getPassword())) {
            log.warn("Failed admin login attempt for email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }

        Admin admin = adminOpt.get();
        log.info("Admin login successful for email: {}. Generating session token.", admin.getEmail());
        String token = jwtTokenProvider.generateToken(admin.getId(), admin.getEmail(), "ROLE_ADMIN");

        return ResponseEntity.ok(AdminAuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(admin.getEmail())
                .build());
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset OTP via email")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody AdminForgotRequest request) {
        log.info("Received admin forgot-password request for email: {}", request.getEmail());

        if (!adminRepository.existsByEmail(request.getEmail())) {
            log.warn("Forgot-password requested for non-existent admin email: {}", request.getEmail());
            // Return OK anyway to prevent email enumeration
            return ResponseEntity.ok(Map.of("message", "If that account exists, an OTP has been sent."));
        }

        boolean sent = otpService.sendOtp(request.getEmail(), OtpPurpose.ADMIN_PASSWORD_RESET, request.isResend());
        if (!sent) {
            log.info("OTP cooldown active for admin password reset: {}", request.getEmail());
            return ResponseEntity.ok(Map.of("message", "If that account exists, an OTP was already sent recently. Please check your email."));
        }
        
        log.info("Successfully sent admin password reset OTP to: {}", request.getEmail());
        return ResponseEntity.ok(Map.of("message", "If that account exists, an OTP has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using OTP")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody AdminResetPasswordRequest request) {
        log.info("Received admin reset-password request for email: {}", request.getEmail());

        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp(), OtpPurpose.ADMIN_PASSWORD_RESET);
        if (!isValid) {
            log.warn("Invalid OTP attempt for admin password reset: {}", request.getEmail());
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP"));
        }

        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("OTP verified but admin not found in DB for email: {}", request.getEmail());
                    return new RuntimeException("Admin not found");
                });

        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        adminRepository.save(admin);

        log.info("Admin password successfully reset for email: {}", request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Password has been successfully reset. You can now login."));
    }

    @PostMapping("/login-otp/request")
    @Operation(summary = "Request OTP for Admin passwordless login")
    public ResponseEntity<?> requestLoginOtp(@Valid @RequestBody com.blogapp.admin.dto.request.AdminLoginOtpRequest request) {
        if (!adminRepository.existsByEmail(request.getEmail())) {
            // Return OK anyway to prevent email enumeration
            return ResponseEntity.ok(Map.of("message", "If that account exists, an OTP has been sent."));
        }

        boolean sent = otpService.sendOtp(request.getEmail(), OtpPurpose.ADMIN_LOGIN, request.isResend());
        if (!sent) {
            return ResponseEntity.ok(Map.of("message", "If that account exists, an OTP was already sent recently. Please check your email."));
        }
        return ResponseEntity.ok(Map.of("message", "If that account exists, an OTP has been sent."));
    }

    @PostMapping("/login-otp/verify")
    @Operation(summary = "Verify OTP to login admin")
    public ResponseEntity<?> verifyLoginOtp(@Valid @RequestBody com.blogapp.admin.dto.request.AdminVerifyOtpLoginRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp(), OtpPurpose.ADMIN_LOGIN);
        if (!isValid) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP"));
        }

        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        String token = jwtTokenProvider.generateToken(admin.getId(), admin.getEmail(), "ROLE_ADMIN");

        return ResponseEntity.ok(AdminAuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(admin.getEmail())
                .build());
    }
}
