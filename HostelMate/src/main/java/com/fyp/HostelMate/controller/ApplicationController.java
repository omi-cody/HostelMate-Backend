package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.entity.Application;
import com.fyp.HostelMate.entity.enums.ApplicationStatus;
import com.fyp.HostelMate.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/submit")
    public ResponseEntity<Application> submitApplication(@RequestBody Application application) {
        return ResponseEntity.ok(applicationService.submitApplication(application));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Application>> getStudentApplications(@PathVariable UUID studentId) {
        return ResponseEntity.ok(applicationService.getApplicationsByStudent(studentId));
    }

    @GetMapping("/hostel/{hostelId}")
    public ResponseEntity<List<Application>> getHostelApplications(@PathVariable UUID hostelId) {
        return ResponseEntity.ok(applicationService.getApplicationsByHostel(hostelId));
    }

    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<Application> updateStatus(@PathVariable UUID applicationId, @RequestParam ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(applicationId, status));
    }
}
