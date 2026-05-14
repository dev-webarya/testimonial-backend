package com.blogapp.blog.service;

import com.blogapp.blog.entity.BlogPost;

public interface BlogSubscriptionService {

    void requestOtp(String email, boolean isResend);

    void subscribe(String email, String otp);

    void unsubscribe(String email);

    void notifySubscribersAsync(BlogPost post);
}
