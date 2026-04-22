package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.KycVerifyRequest;
import com.fyp.HostelMate.dto.request.UpdateSiteContentRequest;
import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import com.fyp.HostelMate.repository.*;
import com.fyp.HostelMate.service.Impl.AdminServiceImpl;
import com.fyp.HostelMate.service.Impl.SiteContentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminServiceImpl adminService;
    private final SiteContentServiceImpl siteContentService;

    // Injected for reviews + notifications + chart data
    private final HostelReviewRepository hostelReviewRepo;
    private final StudentReviewRepository studentReviewRepo;
    private final NotificationRepository notificationRepo;
    private final StudentKycRepository studentKycRepo;
    private final HostelKycRepository hostelKycRepo;
    private final AdmissionRepository admissionRepo;
    private final PaymentRepository paymentRepo;

    // DASHBOARD with chart data
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Object>> getDashboard() {
        var stats = adminService.getDashboardStats();

        // Monthly student registrations (last 6 months)
        List<Map<String, Object>> monthlyStudents = getLast6MonthsStudentData();
        List<Map<String, Object>> monthlyHostels = getLast6MonthsHostelData();
        List<Map<String, Object>> studyLevelChart = getStudyLevelDistribution();
        List<Map<String, Object>> hostelTypeChart = getHostelTypeDistribution();
        Map<String, Object> revenueStats = getRevenueStats();

        var result = new HashMap<>(stats);
        result.put("monthlyStudents", monthlyStudents);
        result.put("monthlyHostels", monthlyHostels);
        result.put("studyLevelChart", studyLevelChart);
        result.put("hostelTypeChart", hostelTypeChart);
        result.put("revenueStats", revenueStats);

        return ResponseEntity.ok(ApiResponse.success("Dashboard stats", result));
    }

    // NOTIFICATIONS (KYC submissions)
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<Object>> getAdminNotifications() {
        // Build admin notifications from pending KYC submissions
        List<Map<String, Object>> notifs = new ArrayList<>();

        studentKycRepo.findByKycStatus(VerificationStatus.SUBMITTED).forEach(k -> {
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("id", k.getKycId());
            n.put("type", "STUDENT_KYC");
            n.put("message", "Student KYC submitted: " + k.getStudent().getUser().getFullName());
            n.put("submittedAt", k.getSubmittedAt());
            n.put("link", "/admin/kyc/students");
            notifs.add(n);
        });

        hostelKycRepo.findByKycStatus(VerificationStatus.SUBMITTED).forEach(k -> {
            Map<String, Object> n = new LinkedHashMap<>();
            n.put("id", k.getKycId());
            n.put("type", "HOSTEL_KYC");
            n.put("message", "Hostel KYC submitted: " + k.getHostel().getHostelName());
            n.put("submittedAt", k.getSubmittedAt());
            n.put("link", "/admin/kyc/hostels");
            notifs.add(n);
        });

        // Sort by submittedAt descending
        notifs.sort((a, b) -> {
            if (a.get("submittedAt") == null) return 1;
            if (b.get("submittedAt") == null) return -1;
            return b.get("submittedAt").toString().compareTo(a.get("submittedAt").toString());
        });

        return ResponseEntity.ok(ApiResponse.success("Admin notifications", notifs));
    }

    @GetMapping("/notifications/count")
    public ResponseEntity<ApiResponse<Object>> getNotificationCount() {
        long count = studentKycRepo.findByKycStatus(VerificationStatus.SUBMITTED).size()
                + hostelKycRepo.findByKycStatus(VerificationStatus.SUBMITTED).size();
        return ResponseEntity.ok(ApiResponse.success("Unread count", Map.of("count", count)));
    }

    // REVIEWS
    @GetMapping("/reviews/hostels")
    public ResponseEntity<ApiResponse<Object>> getAllHostelReviews() {
        return ResponseEntity.ok(ApiResponse.success("Hostel reviews", hostelReviewRepo.findAll()));
    }

    @GetMapping("/reviews/students")
    public ResponseEntity<ApiResponse<Object>> getAllStudentReviews() {
        return ResponseEntity.ok(ApiResponse.success("Student reviews", studentReviewRepo.findAll()));
    }

    //SITE CONTENT
    @GetMapping("/site-content")
    public ResponseEntity<ApiResponse<Object>> getSiteContent() {
        return ResponseEntity.ok(ApiResponse.success("Site content", siteContentService.getSiteContent()));
    }

    @PutMapping("/site-content")
    public ResponseEntity<ApiResponse<Object>> updateSiteContent(@RequestBody UpdateSiteContentRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Homepage updated", siteContentService.updateSiteContent(req)));
    }

    //KYC
    @GetMapping("/kyc/students/pending")
    public ResponseEntity<ApiResponse<Object>> getPendingStudentKyc() {
        return ResponseEntity.ok(ApiResponse.success("Pending student KYC list", adminService.getAllPendingStudentKyc()));
    }

    @PatchMapping("/kyc/students/{kycId}")
    public ResponseEntity<ApiResponse<Void>> verifyStudentKyc(@PathVariable UUID kycId, @Valid @RequestBody KycVerifyRequest req) {
        adminService.verifyStudentKyc(kycId, req);
        return ResponseEntity.ok(ApiResponse.success("Student KYC updated"));
    }

    @GetMapping("/kyc/hostels/pending")
    public ResponseEntity<ApiResponse<Object>> getPendingHostelKyc() {
        return ResponseEntity.ok(ApiResponse.success("Pending hostel KYC list", adminService.getAllPendingHostelKyc()));
    }

    @PatchMapping("/kyc/hostels/{kycId}")
    public ResponseEntity<ApiResponse<Void>> verifyHostelKyc(@PathVariable UUID kycId, @Valid @RequestBody KycVerifyRequest req) {
        adminService.verifyHostelKyc(kycId, req);
        return ResponseEntity.ok(ApiResponse.success("Hostel KYC updated"));
    }

    //LISTS
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<Object>> getAllStudents() {
        return ResponseEntity.ok(ApiResponse.success("All students", adminService.getAllStudents()));
    }

    @GetMapping("/hostels")
    public ResponseEntity<ApiResponse<Object>> getAllHostels() {
        return ResponseEntity.ok(ApiResponse.success("All hostels", adminService.getAllHostels()));
    }

    // CHART DATA HELPERS
    private List<Map<String, Object>> getLast6MonthsStudentData() {
        // Simplified: count KYC submissions per month
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            String label = month.getMonth().toString().substring(0, 3) + " " + month.getYear();
            long count = studentKycRepo.findAll().stream()
                    .filter(k -> k.getSubmittedAt() != null &&
                            k.getSubmittedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate().getYear() == month.getYear() &&
                            k.getSubmittedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate().getMonthValue() == month.getMonthValue())
                    .count();
            result.add(Map.of("month", label, "count", count));
        }
        return result;
    }

    private List<Map<String, Object>> getLast6MonthsHostelData() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            String label = month.getMonth().toString().substring(0, 3) + " " + month.getYear();
            long count = hostelKycRepo.findAll().stream()
                    .filter(k -> k.getSubmittedAt() != null &&
                            k.getSubmittedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate().getYear() == month.getYear() &&
                            k.getSubmittedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate().getMonthValue() == month.getMonthValue())
                    .count();
            result.add(Map.of("month", label, "count", count));
        }
        return result;
    }

    private List<Map<String, Object>> getStudyLevelDistribution() {
        Map<String, Long> grouped = studentKycRepo.findAll().stream()
                .filter(k -> k.getLevelOfStudy() != null)
                .collect(Collectors.groupingBy(k -> k.getLevelOfStudy(), Collectors.counting()));
        return grouped.entrySet().stream()
                .map(e -> Map.of("level", (Object)e.getKey(), "count", (Object)e.getValue()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getHostelTypeDistribution() {
        Map<String, Long> grouped = adminService.getAllHostels().stream()
                .collect(Collectors.groupingBy(h -> h.getHostelType().name(), Collectors.counting()));
        return grouped.entrySet().stream()
                .map(e -> Map.of("type", (Object)e.getKey(), "count", (Object)e.getValue()))
                .collect(Collectors.toList());
    }

    private Map<String, Object> getRevenueStats() {
        var allPayments = paymentRepo.findAll();
        var paid = allPayments.stream()
                .filter(p -> p.getStatus().name().equals("PAID"))
                .collect(Collectors.toList());
        double total = paid.stream().mapToDouble(p -> p.getAmount().doubleValue()).sum();
        return Map.of("totalRevenue", total, "totalPayments", paid.size());
    }

    // Get admitted students for a specific hostel (for admin detail view)
    @GetMapping("/hostels/{hostelId}/admissions")
    public ResponseEntity<ApiResponse<Object>> getHostelAdmissions(@PathVariable UUID hostelId) {
        var admissions = admissionRepo.findByHostel_HostelIdAndStatusOrderByAdmittedDateDesc(
                hostelId, com.fyp.HostelMate.entity.enums.AdmissionStatus.ACTIVE);
        var pending = admissionRepo.findByHostel_HostelIdAndStatusOrderByAdmittedDateDesc(
                hostelId, com.fyp.HostelMate.entity.enums.AdmissionStatus.PENDING_PAYMENT);
        var all = new java.util.ArrayList<>(pending);
        all.addAll(admissions);
        return ResponseEntity.ok(ApiResponse.success("Hostel admissions", all));
    }

}
