package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.entity.Payment;
import com.fyp.HostelMate.entity.enums.PaymentStatus;
import com.fyp.HostelMate.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.createPayment(payment));
    }

    @PatchMapping("/{paymentId}/status")
    public ResponseEntity<Payment> updateStatus(@PathVariable UUID paymentId, @RequestParam PaymentStatus status) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, status));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Payment>> getStudentPayments(@PathVariable UUID studentId) {
        return ResponseEntity.ok(paymentService.getPaymentsByStudent(studentId));
    }

    @GetMapping("/hostel/{hostelId}")
    public ResponseEntity<List<Payment>> getHostelPayments(@PathVariable UUID hostelId) {
        return ResponseEntity.ok(paymentService.getPaymentsByHostel(hostelId));
    }
}
