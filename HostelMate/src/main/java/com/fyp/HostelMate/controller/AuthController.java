package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.*;
import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.dto.response.AuthResponse;
import com.fyp.HostelMate.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse<Void>> registerStudent(
            @Valid @RequestBody StudentRegistrationRequest req) {
        authService.registerStudent(req);
        return ResponseEntity.ok(ApiResponse.success(
                "Student registered successfully. Please complete your KYC to activate your account."));
    }

    @PostMapping("/register/hostel")
    public ResponseEntity<ApiResponse<Void>> registerHostel(
            @Valid @RequestBody HostelRegistrationRequest req) {
        authService.registerHostel(req);
        return ResponseEntity.ok(ApiResponse.success(
                "Hostel registered successfully. Please complete your KYC to get verified."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest req) {
        AuthResponse response = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    // Step 1: user enters their email, we send OTP
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendForgotPasswordOtp(
            @Valid @RequestBody ForgotPasswordRequest req) {
        authService.sendForgotPasswordOtp(req);
        return ResponseEntity.ok(ApiResponse.success(
                "OTP sent to your email. It expires in 5 minutes."));
    }

    // Step 2: user enters the OTP to confirm they own the email
    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest req) {
        boolean valid = authService.verifyOtp(req);
        if (!valid) return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid or expired OTP"));
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully"));
    }

    // Step 3: user sets their new password
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordWithOtpRequest req) {
        authService.resetPasswordWithOtp(req);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. Please log in."));
    }
}
