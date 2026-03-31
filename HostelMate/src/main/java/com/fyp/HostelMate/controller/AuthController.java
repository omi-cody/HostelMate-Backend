package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.ForgotPasswordRequest;
import com.fyp.HostelMate.dto.request.HostelRegistrationRequest;
import com.fyp.HostelMate.dto.request.LoginRequest;
import com.fyp.HostelMate.dto.request.ResetPasswordWithOtpRequest;
import com.fyp.HostelMate.dto.request.StudentRegistrationRequest;
import com.fyp.HostelMate.dto.request.VerifyOtpRequest;
import com.fyp.HostelMate.dto.response.AuthResponse;
import com.fyp.HostelMate.service.Impl.AuthServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthServiceImpl service;

    public AuthController(AuthServiceImpl service) {
        this.service = service;
    }

    @GetMapping("/test")
    public String test() {
        return "Public endpoint works!";
    }

    @PostMapping("/register/student")
    public ResponseEntity<?> registerStudent(
            @Valid @RequestBody StudentRegistrationRequest req) {
        service.registerStudent(req);
        return ResponseEntity.ok(Map.of("message", "Student registered successfully!"));
    }

    @PostMapping("/register/hostel")
    public ResponseEntity<?> registerHostel(
            @Valid @RequestBody HostelRegistrationRequest req) {
        service.registerHostel(req);
        return ResponseEntity.ok(Map.of("message", "Hostel registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(service.login(req));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request){
        return service.forgotPassword(request);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request){
        return service.verifyOtp(request);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordWithOtpRequest request){
        return service.resetPasswordWithOtp(request);
    }
}
