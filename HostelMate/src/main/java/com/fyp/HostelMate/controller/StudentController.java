package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.StudentKycRequest;
import com.fyp.HostelMate.dto.request.StudentUpdateRequest;
import com.fyp.HostelMate.dto.response.StudentProfileResponse;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * POST /api/student/kyc
     * Multipart form — student submits additional profile + documents for admin verification.
     */
    @PostMapping(value = "/kyc", consumes = "multipart/form-data")
    public ResponseEntity<?> submitKyc(
            @AuthenticationPrincipal User currentUser,
            @ModelAttribute StudentKycRequest request) {

        studentService.submitKyc(currentUser, request);
        return ResponseEntity.ok(Map.of("message", "KYC submitted successfully. Awaiting admin verification."));
    }

    /**
     * GET /api/student/profile
     * Returns the full profile of the currently logged-in student.
     */
    @GetMapping("/profile")
    public ResponseEntity<StudentProfileResponse> getProfile(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(studentService.getProfile(currentUser));
    }

    /**
     * PATCH /api/student/profile
     * Update only the mutable fields. DOB, document info, and permanent address are locked.
     */
    @PatchMapping("/profile")
    public ResponseEntity<StudentProfileResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody StudentUpdateRequest request) {

        return ResponseEntity.ok(studentService.updateProfile(currentUser, request));
    }
}
