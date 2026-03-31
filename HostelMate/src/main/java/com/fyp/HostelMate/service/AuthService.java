package com.fyp.HostelMate.service;

import com.fyp.HostelMate.dto.request.ForgotPasswordRequest;
import com.fyp.HostelMate.dto.response.AuthResponse;
import com.fyp.HostelMate.dto.request.HostelRegistrationRequest;
import com.fyp.HostelMate.dto.request.LoginRequest;
import com.fyp.HostelMate.dto.request.StudentRegistrationRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    public void registerStudent(StudentRegistrationRequest studentRegistrationRequest);
    public void registerHostel(HostelRegistrationRequest hostelRegistrationRequest);
    public AuthResponse login (LoginRequest loginRequest);
    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    public ResponseEntity<?> verifyOtp(com.fyp.HostelMate.dto.request.VerifyOtpRequest request);
    public ResponseEntity<?> resetPasswordWithOtp(com.fyp.HostelMate.dto.request.ResetPasswordWithOtpRequest request);
}
