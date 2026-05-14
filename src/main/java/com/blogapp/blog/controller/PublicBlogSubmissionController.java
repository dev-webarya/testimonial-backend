package com.blogapp.blog.controller;

import com.blogapp.blog.dto.request.CreateBlogRequest;
import com.blogapp.blog.dto.request.SubmissionRequest;
import com.blogapp.blog.dto.response.BlogDetailResponse;
import com.blogapp.blog.entity.BlogPost;
import com.blogapp.blog.mapper.BlogMapper;
import com.blogapp.blog.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/blogs/submissions")
@RequiredArgsConstructor
@Tag(name = "Public Blog Submission", description = "Endpoints for guest blog submission with email OTP verification")
public class PublicBlogSubmissionController {

    private final BlogService blogService;
    private final BlogMapper blogMapper;

    @PostMapping("/start")
    @Operation(summary = "Start submission", description = "Sends an OTP to the author's email to verify ownership before submission.")
    public ResponseEntity<Map<String, Object>> startSubmission(@Valid @RequestBody SubmissionRequest.Start request) {
        blogService.startSubmission(request.getAuthorEmail(), request.isResend());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OTP sent to " + request.getAuthorEmail()
        ));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify OTP", description = "Verifies the OTP sent to the author's email.")
    public ResponseEntity<Map<String, Object>> verifySubmission(@Valid @RequestBody SubmissionRequest.Verify request) {
        boolean isVerified = blogService.verifySubmission(request.getAuthorEmail(), request.getOtp());
        return ResponseEntity.ok(Map.of(
                "success", isVerified,
                "verified", isVerified
        ));
    }

    @PostMapping("/finish")
    @Operation(summary = "Finish submission", description = "Creates the blog post in PENDING status. Requires email to be verified first.")
    public ResponseEntity<BlogDetailResponse> finishSubmission(@Valid @RequestBody CreateBlogRequest request) {
        BlogPost blog = blogService.finishSubmission(request);
        return ResponseEntity.ok(blogMapper.toDetailResponse(blog));
    }
}
