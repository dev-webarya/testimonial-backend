package com.blogapp.otp.service;

import com.blogapp.otp.enums.OtpPurpose;

public interface OtpService {

    boolean sendOtp(String email, OtpPurpose purpose, boolean isResend);

    boolean verifyOtp(String email, String otp, OtpPurpose purpose);

    boolean isEmailVerified(String email, OtpPurpose purpose);
}
