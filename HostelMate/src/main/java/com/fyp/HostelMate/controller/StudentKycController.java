package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.StudentKycRequest;
import com.fyp.HostelMate.dto.request.UpdateStudentProfileRequest;
import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.service.StudentKycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentKycController {

    private final StudentKycService studentKycService;

    // Submit KYC for the first time after registration
    @PostMapping("/kyc/submit")
    public ResponseEntity<ApiResponse<Void>> submitKyc(
            Authentication auth,
            @Valid @RequestBody StudentKycRequest req) {
        studentKycService.submitKyc(auth.getName(), req);
        return ResponseEntity.ok(ApiResponse.success(
                "KYC submitted successfully. Please wait for admin verification."));
    }

    // Resubmit after admin rejected - replaces all KYC fields with corrected data
    @PutMapping("/kyc/resubmit")
    public ResponseEntity<ApiResponse<Void>> resubmitKyc(
            Authentication auth,
            @Valid @RequestBody StudentKycRequest req) {
        studentKycService.resubmitKyc(auth.getName(), req);
        return ResponseEntity.ok(ApiResponse.success(
                "KYC resubmitted. Please wait for admin verification."));
    }

    // Student checks their own KYC status and data
    @GetMapping("/kyc/me")
    public ResponseEntity<ApiResponse<Object>> getMyKyc(Authentication auth) {
        var kyc = studentKycService.getMyKyc(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("KYC fetched", kyc));
    }

    // Update mutable profile fields (name, phone, photo, guardian, institute)
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            Authentication auth,
            @Valid @RequestBody UpdateStudentProfileRequest req) {
        studentKycService.updateProfile(auth.getName(), req);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully"));
    }
}
