package com.fyp.HostelMate.service;

import com.fyp.HostelMate.dto.request.*;
import com.fyp.HostelMate.dto.response.AuthResponse;

public interface AuthService {

    void registerStudent(StudentRegistrationRequest request);

    void registerHostel(HostelRegistrationRequest request);

    AuthResponse login(LoginRequest request);

    // Step 1: send OTP to email
    void sendForgotPasswordOtp(ForgotPasswordRequest request);

    // Step 2: verify OTP is correct (returns success/failure)
    boolean verifyOtp(VerifyOtpRequest request);

    // Step 3: set the new password after OTP confirmed
    void resetPasswordWithOtp(ResetPasswordWithOtpRequest request);
}
