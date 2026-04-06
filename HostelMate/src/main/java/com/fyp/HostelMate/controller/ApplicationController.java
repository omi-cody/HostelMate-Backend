package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.ApplicationRequest;
import com.fyp.HostelMate.dto.request.ApplicationStatusRequest;
import com.fyp.HostelMate.dto.response.AdmissionResponse;
import com.fyp.HostelMate.dto.response.ApplicationResponse;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.entity.enums.MealPreference;
import com.fyp.HostelMate.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // ── STUDENT ENDPOINTS ─────────────────────────────────────────────────────

    /**
     * POST /api/student/applications
     * Student applies to a hostel for a visit or direct admission.
     */
    @PostMapping("/api/student/applications")
    public ResponseEntity<ApplicationResponse> apply(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.apply(currentUser, request));
    }

    /**
     * GET /api/student/applications
     * Student views all their applications across hostels.
     */
    @GetMapping("/api/student/applications")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(applicationService.getMyApplications(currentUser));
    }

    /**
     * DELETE /api/student/applications/{id}
     * Student cancels a PENDING application.
     */
    @DeleteMapping("/api/student/applications/{id}")
    public ResponseEntity<?> cancelApplication(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        applicationService.cancelApplication(currentUser, id);
        return ResponseEntity.ok(Map.of("message", "Application cancelled."));
    }

    /**
     * GET /api/student/admission
     * Student views their current active admission (room, roommates, fee, meal plan).
     */
    @GetMapping("/api/student/admission")
    public ResponseEntity<AdmissionResponse> getMyAdmission(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(applicationService.getMyAdmission(currentUser));
    }

    /**
     * PATCH /api/student/admission/meal-preference
     * Student sets their meal preference (VEG / NON_VEG).
     */
    @PatchMapping("/api/student/admission/meal-preference")
    public ResponseEntity<AdmissionResponse> updateMealPreference(
            @AuthenticationPrincipal User currentUser,
            @RequestParam MealPreference preference) {
        return ResponseEntity.ok(applicationService.updateMealPreference(currentUser, preference));
    }

    // ── HOSTEL ENDPOINTS ──────────────────────────────────────────────────────

    /**
     * GET /api/hostel/applications
     * Hostel views all incoming applications.
     */
    @GetMapping("/api/hostel/applications")
    public ResponseEntity<List<ApplicationResponse>> getIncomingApplications(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(applicationService.getIncomingApplications(currentUser));
    }

    /**
     * PATCH /api/hostel/applications/{id}/status
     * Hostel approves (+ assigns room or schedules visit) or rejects an application.
     */
    @PatchMapping("/api/hostel/applications/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @RequestBody ApplicationStatusRequest request) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(currentUser, id, request));
    }

    /**
     * GET /api/hostel/admissions
     * Hostel views the list of all currently admitted students.
     */
    @GetMapping("/api/hostel/admissions")
    public ResponseEntity<List<AdmissionResponse>> getHostelAdmissions(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(applicationService.getHostelAdmissions(currentUser));
    }
}
