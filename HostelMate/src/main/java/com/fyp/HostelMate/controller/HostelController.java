package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.HostelKycRequest;
import com.fyp.HostelMate.dto.request.HostelUpdateRequest;
import com.fyp.HostelMate.dto.response.HostelProfileResponse;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.service.HostelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class HostelController {

    private final HostelService hostelService;

    // ── PUBLIC endpoints (no auth needed) ─────────────────────────────────────

    /**
     * GET /api/hostels
     * Returns all verified hostels — used by students to browse.
     */
    @GetMapping("/api/hostels")
    public ResponseEntity<List<HostelProfileResponse>> listHostels() {
        return ResponseEntity.ok(hostelService.listVerifiedHostels());
    }

    /**
     * GET /api/hostels/{id}
     * Public hostel detail page.
     */
    @GetMapping("/api/hostels/{id}")
    public ResponseEntity<HostelProfileResponse> getHostelDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(hostelService.getPublicProfile(id));
    }

    // ── HOSTEL-ONLY endpoints ─────────────────────────────────────────────────

    /**
     * POST /api/hostel/kyc
     * Hostel submits additional profile + documents for admin verification.
     */
    @PostMapping(value = "/api/hostel/kyc", consumes = "multipart/form-data")
    public ResponseEntity<?> submitKyc(
            @AuthenticationPrincipal User currentUser,
            @ModelAttribute HostelKycRequest request) {

        hostelService.submitKyc(currentUser, request);
        return ResponseEntity.ok(Map.of("message", "KYC submitted successfully. Awaiting admin verification."));
    }

    /**
     * GET /api/hostel/profile
     * Returns the full profile of the currently logged-in hostel.
     */
    @GetMapping("/api/hostel/profile")
    public ResponseEntity<HostelProfileResponse> getProfile(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(hostelService.getProfile(currentUser));
    }

    /**
     * PATCH /api/hostel/profile
     * Update mutable fields. Registration number, PAN, and registration photo are locked.
     */
    @PatchMapping(value = "/api/hostel/profile", consumes = "multipart/form-data")
    public ResponseEntity<HostelProfileResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @ModelAttribute HostelUpdateRequest request) {

        return ResponseEntity.ok(hostelService.updateProfile(currentUser, request));
    }
}
