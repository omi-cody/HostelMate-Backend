package com.fyp.HostelMate.service;

import com.fyp.HostelMate.entity.Payment;
import com.fyp.HostelMate.entity.enums.PaymentStatus;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    Payment createPayment(Payment payment);
    Payment updatePaymentStatus(UUID paymentId, PaymentStatus status);
    List<Payment> getPaymentsByStudent(UUID studentId);
    List<Payment> getPaymentsByHostel(UUID hostelId);
}
