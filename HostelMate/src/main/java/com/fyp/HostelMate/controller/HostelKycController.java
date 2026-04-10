package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.HostelKycRequest;
import com.fyp.HostelMate.dto.request.UpdateHostelProfileRequest;
import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.service.Impl.HostelKycServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hostel")
@RequiredArgsConstructor
@PreAuthorize("hasRole('HOSTEL')")
public class HostelKycController {

    private final HostelKycServiceImpl hostelKycService;

    @PostMapping("/kyc/submit")
    public ResponseEntity<ApiResponse<Void>> submitKyc(
            Authentication auth,
            @Valid @RequestBody HostelKycRequest req) {
        hostelKycService.submitKyc(auth.getName(), req);
        return ResponseEntity.ok(ApiResponse.success(
                "KYC submitted. Awaiting admin verification."));
    }

    @PutMapping("/kyc/resubmit")
    public ResponseEntity<ApiResponse<Void>> resubmitKyc(
            Authentication auth,
            @Valid @RequestBody HostelKycRequest req) {
        hostelKycService.resubmitKyc(auth.getName(), req);
        return ResponseEntity.ok(ApiResponse.success(
                "KYC resubmitted. Awaiting admin verification."));
    }

    @GetMapping("/kyc/me")
    public ResponseEntity<ApiResponse<Object>> getMyKyc(Authentication auth) {
        var kyc = hostelKycService.getMyKyc(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("KYC fetched", kyc));
    }

    // Update logo, amenities, rules, and meal plan (PAN stays locked)
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            Authentication auth,
            @RequestBody UpdateHostelProfileRequest req) {
        hostelKycService.updateProfile(auth.getName(), req);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully"));
    }
}
