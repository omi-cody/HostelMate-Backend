package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.LeaveRemarkRequest;
import com.fyp.HostelMate.dto.request.ReviewRequest;
import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.service.Impl.AdmissionServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AdmissionController {

    private final AdmissionServiceImpl admissionService;

    // STUDENT

    @GetMapping("/api/student/my-hostel")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> getMyActiveAdmission(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Active admission",
                admissionService.getMyActiveAdmission(auth.getName())));
    }

    // Student clicks "Leave" button on their hostel page
    @PostMapping("/api/student/my-hostel/request-leave")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> requestLeave(Authentication auth) {
        admissionService.requestLeave(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(
                "Leave request sent to hostel. Awaiting approval."));
    }

    // Student leaves a review for the hostel after leave is accepted
    @PostMapping("/api/student/admissions/{admissionId}/review")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> reviewHostel(
            Authentication auth,
            @PathVariable UUID admissionId,
            @Valid @RequestBody ReviewRequest req) {
        admissionService.submitHostelReview(auth.getName(), admissionId, req);
        return ResponseEntity.ok(ApiResponse.success("Review submitted. Thank you!"));
    }

    // Student dashboard  summary of current stay, fees, complaints
    @GetMapping("/api/student/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> getStudentDashboard(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Dashboard",
                admissionService.getStudentDashboard(auth.getName())));
    }

    // HOSTEL

    // Hostel accepts or rejects a student's leave request
    @PatchMapping("/api/hostel/admissions/{admissionId}/leave-response")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Void>> respondToLeave(
            Authentication auth,
            @PathVariable UUID admissionId,
            @Valid @RequestBody LeaveRemarkRequest req) {
        admissionService.respondToLeave(auth.getName(), admissionId, req);
        return ResponseEntity.ok(ApiResponse.success("Leave response submitted"));
    }

    // Hostel rates the student after leave is accepted
    @PostMapping("/api/hostel/admissions/{admissionId}/review")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Void>> reviewStudent(
            Authentication auth,
            @PathVariable UUID admissionId,
            @Valid @RequestBody ReviewRequest req) {
        admissionService.submitStudentReview(auth.getName(), admissionId, req);
        return ResponseEntity.ok(ApiResponse.success("Student review submitted"));
    }

    // Hostel dashboard overview
    @GetMapping("/api/hostel/dashboard")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Object>> getHostelDashboard(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Dashboard",
                admissionService.getHostelDashboard(auth.getName())));
    }
}
