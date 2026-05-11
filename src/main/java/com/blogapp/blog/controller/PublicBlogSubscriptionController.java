package com.blogapp.blog.controller;

import com.blogapp.blog.dto.request.SubscriptionRequest;
import com.blogapp.blog.service.BlogSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/blogs/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Public Blog Subscription", description = "Endpoints for subscribing to new blog post updates")
public class PublicBlogSubscriptionController {

    private final BlogSubscriptionService subscriptionService;

    @PostMapping("/request-otp")
    @Operation(summary = "Request OTP for subscription", description = "Sends an OTP to the provided email address")
    public ResponseEntity<Map<String, Object>> requestOtp(
            @Valid @RequestBody SubscriptionRequest.Start request) {
        subscriptionService.requestOtp(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OTP sent to " + request.getEmail()
        ));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify OTP and subscribe", description = "Verifies the OTP and activates the subscription")
    public ResponseEntity<Map<String, Object>> subscribe(
            @Valid @RequestBody SubscriptionRequest.Verify request) {
        subscriptionService.subscribe(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully subscribed to blog updates"
        ));
    }

    @PostMapping("/unsubscribe")
    @Operation(summary = "Unsubscribe from blog updates", description = "Deactivates the subscription for the given email")
    public ResponseEntity<Map<String, Object>> unsubscribe(
            @Valid @RequestBody SubscriptionRequest.Unsubscribe request) {
        subscriptionService.unsubscribe(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully unsubscribed from blog updates"
        ));
    }
}
