package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import com.fyp.HostelMate.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{userId}/approve")
    public ResponseEntity<User> approveUser(@PathVariable UUID userId, @RequestParam VerificationStatus status) {
        return ResponseEntity.ok(adminService.approveUser(userId, status));
    }

    @PatchMapping("/hostels/{hostelId}/approve")
    public ResponseEntity<Hostel> approveHostel(@PathVariable UUID hostelId, @RequestParam VerificationStatus status) {
        return ResponseEntity.ok(adminService.approveHostel(hostelId, status));
    }
}
