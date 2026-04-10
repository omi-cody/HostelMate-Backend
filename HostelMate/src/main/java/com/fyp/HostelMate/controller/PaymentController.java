package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.GenerateInvoiceRequest;
import com.fyp.HostelMate.dto.request.KhaltiPaymentRequest;
import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.service.Impl.PaymentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentServiceImpl paymentService;

    // ── STUDENT PAYMENTS ──────────────────────────────────────────────────

    // Step 1: create a PENDING payment record, returns info for Khalti SDK
    @PostMapping("/api/student/payments/initiate")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> initiateKhaltiPayment(
            Authentication auth,
            @Valid @RequestBody KhaltiPaymentRequest req) {
        var payment = paymentService.initiateKhaltiPayment(auth.getName(), req);
        return ResponseEntity.ok(ApiResponse.success(
                "Payment initiated. Proceed with Khalti.", payment));
    }

    // Step 2: called after Khalti redirects back - verify the pidx with Khalti servers
    @PostMapping("/api/student/payments/{paymentId}/verify")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> verifyKhaltiPayment(
            Authentication auth,
            @PathVariable UUID paymentId,
            @RequestParam String pidx) {
        var payment = paymentService.verifyKhaltiPayment(auth.getName(), paymentId, pidx);
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", payment));
    }

    // Student views their full payment history across all hostels
    @GetMapping("/api/student/payments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Object>> getMyPaymentHistory(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Payment history",
                paymentService.getMyPaymentHistory(auth.getName())));
    }

    // Download payment history as a PDF file
    @GetMapping("/api/student/payments/export-pdf")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<byte[]> exportStudentPaymentPdf(Authentication auth) {
        byte[] pdf = paymentService.exportPaymentHistoryAsPdf(auth.getName(), false);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"payment-history.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ── HOSTEL PAYMENTS ───────────────────────────────────────────────────

    // Hostel records a cash payment and generates an invoice
    @PostMapping("/api/hostel/payments/invoice")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Object>> generateCashInvoice(
            Authentication auth,
            @Valid @RequestBody GenerateInvoiceRequest req) {
        var payment = paymentService.generateCashInvoice(auth.getName(), req);
        return ResponseEntity.ok(ApiResponse.success(
                "Invoice generated. Invoice No: " + payment.getInvoiceNumber(), payment));
    }

    // Hostel views all payment records (retained even after students leave)
    @GetMapping("/api/hostel/payments")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Object>> getHostelPaymentHistory(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Payment history",
                paymentService.getHostelPaymentHistory(auth.getName())));
    }

    // Export financial report as PDF for the hostel
    @GetMapping("/api/hostel/payments/export-pdf")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<byte[]> exportHostelPaymentPdf(Authentication auth) {
        byte[] pdf = paymentService.exportPaymentHistoryAsPdf(auth.getName(), true);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"financial-report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // Hostel: get payment history grouped by student (for fee tracking)
    @GetMapping("/api/hostel/payments/by-student")
    @PreAuthorize("hasRole('HOSTEL')")
    public ResponseEntity<ApiResponse<Object>> getPaymentsByStudent(Authentication auth) {
        var payments = paymentService.getHostelPaymentHistory(auth.getName());
        // Group by student admissionId for per-student tracking
        java.util.Map<String, java.util.List<com.fyp.HostelMate.entity.Payment>> grouped =
            payments.stream()
                .filter(p -> p.getAdmission() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                    p -> p.getAdmission().getAdmissionId().toString()
                ));
        return ResponseEntity.ok(ApiResponse.success("Payments by student", grouped));
    }

    // Admin: get all payments across system
    @GetMapping("/api/admin/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.success("All payments", paymentService.getAllPayments()));
    }

    // Admin: change payment status (e.g. mark PENDING as PAID for cash confirmed outside system)
    @PatchMapping("/api/admin/payments/{paymentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> updatePaymentStatus(
            @PathVariable UUID paymentId,
            @RequestBody java.util.Map<String, String> body) {
        var status = com.fyp.HostelMate.entity.enums.PaymentStatus.valueOf(body.get("status").toUpperCase());
        var updated = paymentService.adminUpdatePaymentStatus(paymentId, status);
        return ResponseEntity.ok(ApiResponse.success("Payment status updated", updated));
    }
}