package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.response.AdminDashboardResponse;
import com.fyp.HostelMate.dto.response.HostelDashboardResponse;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.service.Impl.AdminDashboardServiceImpl;
import com.fyp.HostelMate.service.Impl.HostelDashboardServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final AdminDashboardServiceImpl adminDashboardService;
    private final HostelDashboardServiceImpl hostelDashboardService;

    /** GET /api/admin/dashboard */
    @GetMapping("/api/admin/dashboard")
    public ResponseEntity<AdminDashboardResponse> adminDashboard() {
        return ResponseEntity.ok(adminDashboardService.getDashboard());
    }

    /** GET /api/hostel/dashboard */
    @GetMapping("/api/hostel/dashboard")
    public ResponseEntity<HostelDashboardResponse> hostelDashboard(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(hostelDashboardService.getDashboard(currentUser));
    }
}
