package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.*;
import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.service.Impl.ApplicationServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationServiceImpl applicationService;

    // STUDENT ENDPOINTS

    @PostMapping("/api/student/apply/{hostelId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> applyToHostel(
            Authentication auth,
            @PathVariable UUID hostelId,
            @Valid @RequestBody ApplyRequest req) {
        var app = applicationService.applyToHostel(auth.getName(), hostelId, req);
        return ResponseEntity.ok(ApiResponse.success("Application submitted successfully", app));
    }

    @GetMapping("/api/student/applications")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> getMyApplications(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Your applications",
                applicationService.getMyApplications(auth.getName())));
    }

    // HOSTEL ENDPOINTS

    @GetMapping("/api/hostel/applications")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Object>> getHostelApplications(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Applications received",
                applicationService.getHostelApplications(auth.getName())));
    }

    // Accept a direct admission application and assign a room
    @PostMapping("/api/hostel/applications/{applicationId}/accept")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Void>> acceptApplication(
            Authentication auth,
            @PathVariable UUID applicationId,
            @Valid @RequestBody AcceptApplicationRequest req) {
        applicationService.acceptApplication(auth.getName(), applicationId, req);
        return ResponseEntity.ok(ApiResponse.success("Application accepted and student admitted"));
    }

    // Schedule a visit date for a visit-type application
    @PostMapping("/api/hostel/applications/{applicationId}/schedule-visit")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Void>> scheduleVisit(
            Authentication auth,
            @PathVariable UUID applicationId,
            @Valid @RequestBody ScheduleVisitRequest req) {
        applicationService.scheduleVisit(auth.getName(), applicationId, req);
        return ResponseEntity.ok(ApiResponse.success("Visit scheduled successfully"));
    }

    // After visit has happened - admit the student
    @PostMapping("/api/hostel/applications/{applicationId}/admit-after-visit")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Void>> admitAfterVisit(
            Authentication auth,
            @PathVariable UUID applicationId,
            @Valid @RequestBody AcceptApplicationRequest req) {
        applicationService.admitAfterVisit(auth.getName(), applicationId, req);
        return ResponseEntity.ok(ApiResponse.success("Student admitted after visit"));
    }

    // Reject or cancel an application
    @PostMapping("/api/hostel/applications/{applicationId}/reject")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Void>> rejectApplication(
            Authentication auth,
            @PathVariable UUID applicationId,
            @RequestBody RejectApplicationRequest req) {
        applicationService.rejectApplication(auth.getName(), applicationId, req);
        return ResponseEntity.ok(ApiResponse.success("Application rejected"));
    }

    // Hostel deletes/cancels an application record (no financial effect - only status PENDING/REJECTED)
    @DeleteMapping("/api/hostel/applications/{applicationId}")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(
            Authentication auth,
            @PathVariable UUID applicationId) {
        applicationService.deleteApplication(auth.getName(), applicationId);
        return ResponseEntity.ok(ApiResponse.success("Application deleted"));
    }

    // Student cancels their own application (only PENDING or ACCEPTED-not-yet-admitted)
    @PatchMapping("/api/student/applications/{applicationId}/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> cancelApplication(
            Authentication auth,
            @PathVariable UUID applicationId) {
        applicationService.cancelApplication(auth.getName(), applicationId);
        return ResponseEntity.ok(ApiResponse.success("Application cancelled"));
    }

    // Hostel allocates room to student AFTER fee payment is confirmed
    @PostMapping("/api/hostel/admissions/{admissionId}/allocate-room")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Void>> allocateRoom(
            Authentication auth,
            @PathVariable UUID admissionId,
            @RequestBody java.util.Map<String, String> body) {
        UUID roomId = UUID.fromString(body.get("roomId"));
        applicationService.allocateRoom(auth.getName(), admissionId, roomId);
        return ResponseEntity.ok(ApiResponse.success("Room allocated successfully"));
    }
}