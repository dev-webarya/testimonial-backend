package com.blogapp.user.service.impl;

import com.blogapp.user.entity.User;
import com.blogapp.user.repository.UserRepository;
import com.blogapp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User findOrCreateByEmail(String email) {
        String cleanEmail = email.toLowerCase().trim();
        return userRepository.findByEmail(cleanEmail)
                .map(user -> {
                    log.info("Found existing user with email: {}", cleanEmail);
                    return user;
                })
                .orElseGet(() -> {
                    log.info("No existing user found for email: {}. Creating new user record.", cleanEmail);
                    return userRepository.save(
                            User.builder()
                                    .email(cleanEmail)
                                    .createdAt(LocalDateTime.now())
                                    .build());
                });
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim());
    }

    @Override
    public User markEmailVerified(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        if (user.getEmailVerifiedAt() == null) {
            user.setEmailVerifiedAt(LocalDateTime.now());
            log.info("Email verified for user ID: {}", userId);
        } else {
            log.debug("Email already verified for user ID: {}", userId);
        }
        
        return userRepository.save(user);
    }

    @Override
    public User updateProfile(String userId, String name, String mobile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        if (name != null)
            user.setName(name);
        if (mobile != null)
            user.setMobile(mobile);
            
        log.info("Updated profile for user ID: {}", userId);
        return userRepository.save(user);
    }

    @Override
    public void updatePassword(String userId, String encodedPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setPassword(encodedPassword);
        userRepository.save(user);
        log.info("Updated password for user ID: {}", userId);
    }
}
