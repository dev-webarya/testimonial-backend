package com.blogapp.media.controller;

import com.blogapp.common.exception.BadRequestException;
import com.blogapp.media.service.CloudinaryService;
import com.blogapp.otp.enums.OtpPurpose;
import com.blogapp.otp.service.OtpService;
import com.blogapp.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Tag(name = "Media Upload", description = "Secure Direct-to-Cloudinary (Signed Upload) endpoints")
public class MediaController {

    private final CloudinaryService cloudinaryService;
    private final OtpService otpService;

    @GetMapping("/signature")
    @Operation(summary = "Get secure Cloudinary upload signature",
            description = "Returns a cryptographically signed ticket allowing the frontend to upload a massive video/image directly to Cloudinary without consuming server bandwidth. Requires a valid JWT User/Admin token. Users must have verified their email OTP for blog submission first.")
    public ResponseEntity<Map<String, Object>> getSignature() {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            // Check if the user has verified their OTP for blog submission
            boolean isVerified = otpService.isEmailVerified(user.getEmail(), OtpPurpose.BLOG_SUBMISSION);
            if (!isVerified) {
                throw new BadRequestException("Please verify your email via OTP before uploading media.");
            }
        }
        
        Map<String, Object> signatureData = cloudinaryService.generateSignature();
        return ResponseEntity.ok(signatureData);
        
    }

    @DeleteMapping
    @Operation(summary = "Delete Cloudinary media", description = "Deletes a previously uploaded media file by its URL. Useful for cleaning up orphaned images if a user changes their feature image before submitting.")
    public ResponseEntity<Void> deleteMedia(@RequestParam String url) {
        cloudinaryService.deleteMediaByUrl(url);
        return ResponseEntity.ok().build();
    }
}

