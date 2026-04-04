package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.entity.MaintenanceRequest;
import com.fyp.HostelMate.entity.enums.MaintenanceStatus;
import com.fyp.HostelMate.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @Autowired
    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping("/submit")
    public ResponseEntity<MaintenanceRequest> submitRequest(@RequestBody MaintenanceRequest request) {
        return ResponseEntity.ok(maintenanceService.submitRequest(request));
    }

    @PatchMapping("/{complaintId}/status")
    public ResponseEntity<MaintenanceRequest> updateStatus(@PathVariable UUID complaintId, @RequestParam MaintenanceStatus status) {
        return ResponseEntity.ok(maintenanceService.updateRequestStatus(complaintId, status));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<MaintenanceRequest>> getStudentRequests(@PathVariable UUID studentId) {
        return ResponseEntity.ok(maintenanceService.getRequestsByStudent(studentId));
    }

    @GetMapping("/hostel/{hostelId}")
    public ResponseEntity<List<MaintenanceRequest>> getHostelRequests(@PathVariable UUID hostelId) {
        return ResponseEntity.ok(maintenanceService.getRequestsByHostel(hostelId));
    }
}
