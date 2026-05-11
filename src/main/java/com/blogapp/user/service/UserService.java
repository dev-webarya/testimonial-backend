package com.blogapp.user.service;

import com.blogapp.user.entity.User;

import java.util.Optional;

public interface UserService {

    /**
     * Find or create a user by email.
     * If the user doesn't exist, creates one with unverified status.
     */
    User findOrCreateByEmail(String email);

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    /**
     * Marks the user's email as verified (sets emailVerifiedAt).
     */
    User markEmailVerified(String userId);

    /**
     * Update user profile fields (name, mobile).
     */
    User updateProfile(String userId, String name, String mobile);

    /**
     * Update user password.
     */
    void updatePassword(String userId, String encodedPassword);
}
