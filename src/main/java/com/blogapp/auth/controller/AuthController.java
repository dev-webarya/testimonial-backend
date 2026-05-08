package com.blogapp.auth.controller;

import com.blogapp.auth.dto.request.AuthStartRequest;
import com.blogapp.auth.dto.request.AuthVerifyRequest;
import com.blogapp.auth.dto.response.AuthResponse;
import com.blogapp.config.JwtTokenProvider;
import com.blogapp.otp.enums.OtpPurpose;
import com.blogapp.otp.service.OtpService;
import com.blogapp.user.entity.User;
import com.blogapp.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
