package com.blogapp.blog.service.impl;

import com.blogapp.blog.entity.BlogPost;
import com.blogapp.blog.entity.BlogSubscription;
import com.blogapp.blog.repository.BlogSubscriptionRepository;
import com.blogapp.blog.service.BlogSubscriptionService;
import com.blogapp.common.exception.BadRequestException;
import com.blogapp.otp.enums.OtpPurpose;
import com.blogapp.otp.service.EmailService;
import com.blogapp.otp.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogSubscriptionServiceImpl implements BlogSubscriptionService {

    private final BlogSubscriptionRepository subscriptionRepository;
    private final OtpService otpService;
    private final EmailService emailService;

    @Value("${app.blog.notifications.enabled:false}")
    private boolean notificationsEnabled;

    @Value("${app.frontend.url:}")
    private String frontendUrl;

    @Override
    public boolean requestOtp(String email, boolean isResend) {
        log.info("Requesting blog subscription OTP for email: {}", email);
        
        Optional<BlogSubscription> existingOpt = subscriptionRepository.findByEmail(email);
        if (existingOpt.isPresent() && existingOpt.get().isActive()) {
            throw new BadRequestException("You are already subscribed to blog updates.");
        }
        
        return otpService.sendOtp(email, OtpPurpose.BLOG_SUBSCRIBE, isResend);
    }

    @Override
    public void subscribe(String email, String otp) {
        if (!otpService.verifyOtp(email, otp, OtpPurpose.BLOG_SUBSCRIBE)) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        Optional<BlogSubscription> existingOpt = subscriptionRepository.findByEmail(email);
        if (existingOpt.isPresent()) {
            BlogSubscription existing = existingOpt.get();
            if (!existing.isActive()) {
                existing.setActive(true);
                subscriptionRepository.save(existing);
                log.info("Re-activated blog subscription for email: {}", email);
            } else {
                log.info("Email {} is already actively subscribed.", email);
            }
        } else {
            BlogSubscription subscription = BlogSubscription.builder()
                    .email(email)
                    .isActive(true)
                    .build();
            subscriptionRepository.save(subscription);
            log.info("Created new blog subscription for email: {}", email);
        }
        
        // Send confirmation email
        try {
            emailService.sendBlogSubscriptionConfirmation(email);
        } catch (Exception e) {
            log.error("Failed to send subscription confirmation email to {}", email, e);
        }
    }

    @Override
    public void unsubscribe(String email) {
        Optional<BlogSubscription> existingOpt = subscriptionRepository.findByEmail(email);
        if (existingOpt.isPresent()) {
            BlogSubscription existing = existingOpt.get();
            if (existing.isActive()) {
                existing.setActive(false);
                subscriptionRepository.save(existing);
                log.info("Deactivated blog subscription for email: {}", email);
            }
        } else {
            log.warn("Attempt to unsubscribe non-existent email: {}", email);
        }
    }

    @Override
    public void notifySubscribersAsync(BlogPost post) {
        if (!notificationsEnabled) {
            log.info("Blog notifications are disabled via feature flag. Skipping subscription dispatch.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                List<BlogSubscription> activeSubscribers = subscriptionRepository.findByIsActiveTrue();
                if (activeSubscribers.isEmpty()) {
                    log.info("No active subscribers found. Notification skipped.");
                    return;
                }

                log.info("Dispatching new blog notifications to {} active subscribers.", activeSubscribers.size());
                
                // Construct the link depending on how the frontend handles blog detail slugs
                String cleanFrontendUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
                String blogLink = cleanFrontendUrl + "/blogs/" + post.getSlug();

                for (BlogSubscription subscriber : activeSubscribers) {
                    try {
                        emailService.sendNewBlogPostNotification(subscriber.getEmail(), post.getTitle(), blogLink);
                    } catch (Exception e) {
                        log.error("Failed to send notification to {}: {}", subscriber.getEmail(), e.getMessage());
                    }
                }
                log.info("Successfully dispatched all notifications for blog: {}", post.getSlug());
            } catch (Exception e) {
                log.error("Critical error during async blog notification dispatch: {}", e.getMessage(), e);
            }
        });
    }
}
