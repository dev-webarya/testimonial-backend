package com.blogapp.auth.controller;

import com.blogapp.auth.dto.response.AuthResponse;
import com.blogapp.blog.service.BlogService;
import com.blogapp.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.blogapp.auth.dto.request.UserChangePasswordRequest;
import com.blogapp.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Logged-in user endpoints")
public class AccountController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final BlogService blogService;

    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Returns the authenticated user's profile info")
    public ResponseEntity<AuthResponse.UserInfo> getMyProfile(@AuthenticationPrincipal User user) {
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .emailVerified(user.getEmailVerifiedAt() != null)
                .build();

        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Allows logged-in users to change their password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal User user,
                                            @Valid @RequestBody UserChangePasswordRequest request) {
        
        User fullUser = userService.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (fullUser.getPassword() != null) {
            if (request.getOldPassword() == null || request.getOldPassword().isBlank() || !passwordEncoder.matches(request.getOldPassword(), fullUser.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Incorrect old password"));
            }
        }

        userService.updatePassword(fullUser.getId(), passwordEncoder.encode(request.getNewPassword()));
        return ResponseEntity.ok(Map.of("message", "Password successfully changed"));
    }

    @GetMapping("/my-blogs")
    @Operation(summary = "Get my submitted blogs", description = "Returns a list of all blogs submitted by the authenticated user")
    public ResponseEntity<java.util.List<com.blogapp.blog.dto.response.BlogSummaryResponse>> getMyBlogs(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(blogService.getMyBlogs(user.getEmail()));
    }
}
