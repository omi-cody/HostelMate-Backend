package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.CashPaymentRequest;
import com.fyp.HostelMate.dto.request.KhaltiVerifyRequest;
import com.fyp.HostelMate.dto.request.PaymentInitiateRequest;
import com.fyp.HostelMate.dto.response.PaymentResponse;
import com.fyp.HostelMate.dto.response.PaymentSummaryResponse;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.service.Impl.PaymentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentServiceImpl paymentService;

    /** POST /api/student/payments/initiate — start a Khalti payment for this month */
    @PostMapping("/api/student/payments/initiate")
    public ResponseEntity<PaymentResponse> initiateKhalti(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PaymentInitiateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiateKhalti(currentUser, request));
    }

    /** POST /api/student/payments/verify — verify Khalti callback and mark payment complete */
    @PostMapping("/api/student/payments/verify")
    public ResponseEntity<PaymentResponse> verifyKhalti(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody KhaltiVerifyRequest request) {
        return ResponseEntity.ok(paymentService.verifyKhalti(currentUser, request));
    }

    /** GET /api/student/payments — student full payment history + totals */
    @GetMapping("/api/student/payments")
    public ResponseEntity<PaymentSummaryResponse> getStudentHistory(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(paymentService.getStudentHistory(currentUser));
    }

    /** POST /api/hostel/payments/cash — hostel records a cash payment, generates invoice */
    @PostMapping("/api/hostel/payments/cash")
    public ResponseEntity<PaymentResponse> recordCash(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CashPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.recordCash(currentUser, request));
    }

    /** GET /api/hostel/payments — hostel views all payments */
    @GetMapping("/api/hostel/payments")
    public ResponseEntity<List<PaymentResponse>> getHostelPayments(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(paymentService.getHostelPayments(currentUser));
    }

    /** GET /api/hostel/payments/pending — hostel views only pending payments */
    @GetMapping("/api/hostel/payments/pending")
    public ResponseEntity<List<PaymentResponse>> getPendingPayments(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(paymentService.getPendingHostelPayments(currentUser));
    }
}
