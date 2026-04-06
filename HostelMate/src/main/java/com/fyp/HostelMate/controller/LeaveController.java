package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.LeaveRequestDto;
import com.fyp.HostelMate.dto.request.LeaveStatusRequest;
import com.fyp.HostelMate.dto.response.LeaveResponse;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.service.Impl.LeaveServiceImpl;
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
public class LeaveController {

    private final LeaveServiceImpl leaveService;

    /** POST /api/student/leave — student requests to leave their hostel */
    @PostMapping("/api/student/leave")
    public ResponseEntity<LeaveResponse> requestLeave(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody LeaveRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveService.requestLeave(currentUser, request));
    }

    /** GET /api/student/leave — student views their leave request history */
    @GetMapping("/api/student/leave")
    public ResponseEntity<List<LeaveResponse>> getMyLeaveRequests(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(leaveService.getMyLeaveRequests(currentUser));
    }

    /** GET /api/hostel/leave — hostel views all leave requests */
    @GetMapping("/api/hostel/leave")
    public ResponseEntity<List<LeaveResponse>> getHostelLeaveRequests(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(leaveService.getHostelLeaveRequests(currentUser));
    }

    /** PATCH /api/hostel/leave/{id} — hostel approves or rejects a leave request */
    @PatchMapping("/api/hostel/leave/{id}")
    public ResponseEntity<LeaveResponse> processLeave(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody LeaveStatusRequest request) {
        return ResponseEntity.ok(leaveService.processLeave(currentUser, id, request));
    }
}
