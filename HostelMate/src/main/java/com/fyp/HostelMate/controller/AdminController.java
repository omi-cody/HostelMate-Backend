package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.response.HostelProfileResponse;
import com.fyp.HostelMate.dto.response.StudentProfileResponse;
import com.fyp.HostelMate.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;


    /** GET /api/admin/students — list all students with their details */
    @GetMapping("/students")
    public ResponseEntity<List<StudentProfileResponse>> getAllStudents() {
        return ResponseEntity.ok(adminService.getAllStudents());
    }

    /** GET /api/admin/students/{id} — single student detail */
    @GetMapping("/students/{id}")
    public ResponseEntity<StudentProfileResponse> getStudent(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getStudent(id));
    }

    /** PATCH /api/admin/students/{id}/verify — approve student KYC */
    @PatchMapping("/students/{id}/verify")
    public ResponseEntity<?> verifyStudent(@PathVariable UUID id) {
        adminService.verifyStudent(id);
        return ResponseEntity.ok(Map.of("message", "Student verified successfully."));
    }

    /** PATCH /api/admin/students/{id}/reject — reject student KYC */
    @PatchMapping("/students/{id}/reject")
    public ResponseEntity<?> rejectStudent(@PathVariable UUID id) {
        adminService.rejectStudent(id);
        return ResponseEntity.ok(Map.of("message", "Student rejected."));
    }

    // ── HOSTELS ───────────────────────────────────────────────────────────────

    /** GET /api/admin/hostels — list all hostels with their details */
    @GetMapping("/hostels")
    public ResponseEntity<List<HostelProfileResponse>> getAllHostels() {
        return ResponseEntity.ok(adminService.getAllHostels());
    }

    /** GET /api/admin/hostels/{id} — single hostel detail */
    @GetMapping("/hostels/{id}")
    public ResponseEntity<HostelProfileResponse> getHostel(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getHostel(id));
    }

    /** PATCH /api/admin/hostels/{id}/verify — approve hostel KYC, makes it publicly listed */
    @PatchMapping("/hostels/{id}/verify")
    public ResponseEntity<?> verifyHostel(@PathVariable UUID id) {
        adminService.verifyHostel(id);
        return ResponseEntity.ok(Map.of("message", "Hostel verified and now publicly listed."));
    }

    /** PATCH /api/admin/hostels/{id}/reject — reject hostel KYC */
    @PatchMapping("/hostels/{id}/reject")
    public ResponseEntity<?> rejectHostel(@PathVariable UUID id) {
        adminService.rejectHostel(id);
        return ResponseEntity.ok(Map.of("message", "Hostel rejected."));
    }
}
