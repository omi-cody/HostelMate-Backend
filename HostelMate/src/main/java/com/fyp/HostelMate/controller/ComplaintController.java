package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.ComplaintCreateRequest;
import com.fyp.HostelMate.dto.request.UpdateComplaintStatusRequest;
import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.service.Impl.ComplaintServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintServiceImpl complaintService;

    // STUDENT

    // Submit a new complaint or maintenance request to the current hostel
    @PostMapping("/api/student/requests")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> submitRequest(
            Authentication auth,
            @Valid @RequestBody ComplaintCreateRequest req) {
        var result = complaintService.submitRequest(auth.getName(), req);
        return ResponseEntity.ok(ApiResponse.success("Request submitted successfully", result));
    }

    // Student tracks all their requests and their current status
    @GetMapping("/api/student/requests")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> getMyRequests(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Your requests",
                complaintService.getMyRequests(auth.getName())));
    }

    // HOSTEL

    // Hostel sees all requests sent to them (pending + in-progress + resolved)
    @GetMapping("/api/hostel/requests")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Object>> getHostelRequests(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("All requests",
                complaintService.getHostelRequests(auth.getName())));
    }

    // Hostel updates progress on a request (PENDING -> IN_PROGRESS -> RESOLVED)
    @PatchMapping("/api/hostel/requests/{requestId}")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            Authentication auth,
            @PathVariable UUID requestId,
            @Valid @RequestBody UpdateComplaintStatusRequest req) {
        complaintService.updateStatus(auth.getName(), requestId, req);
        return ResponseEntity.ok(ApiResponse.success("Request status updated"));
    }

    @DeleteMapping("/api/hostel/requests/{requestId}")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Void>> deleteRequest(Authentication auth, @PathVariable UUID requestId) {
        complaintService.deleteRequest(auth.getName(), requestId);
        return ResponseEntity.ok(ApiResponse.success("Request deleted"));
    }
}