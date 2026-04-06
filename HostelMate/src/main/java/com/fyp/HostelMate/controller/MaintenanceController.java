package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.MaintenanceRequestDto;
import com.fyp.HostelMate.dto.request.MaintenanceStatusRequest;
import com.fyp.HostelMate.dto.response.MaintenanceResponse;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.service.Impl.MaintenanceServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceServiceImpl maintenanceService;

    /** POST /api/student/maintenance — student submits a complaint */
    @PostMapping("/api/student/maintenance")
    public ResponseEntity<MaintenanceResponse> submitComplaint(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody MaintenanceRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(maintenanceService.submitComplaint(currentUser, request));
    }

    /** GET /api/student/maintenance — student tracks their complaints */
    @GetMapping("/api/student/maintenance")
    public ResponseEntity<List<MaintenanceResponse>> getMyComplaints(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(maintenanceService.getMyComplaints(currentUser));
    }

    /** GET /api/hostel/maintenance — hostel views all incoming complaints */
    @GetMapping("/api/hostel/maintenance")
    public ResponseEntity<List<MaintenanceResponse>> getHostelComplaints(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(maintenanceService.getHostelComplaints(currentUser));
    }

    /** PATCH /api/hostel/maintenance/{id}/status — hostel updates complaint status */
    @PatchMapping("/api/hostel/maintenance/{id}/status")
    public ResponseEntity<MaintenanceResponse> updateStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceStatusRequest request) {
        return ResponseEntity.ok(maintenanceService.updateStatus(currentUser, id, request));
    }
}
