package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.entity.Payment;
import com.fyp.HostelMate.entity.enums.PaymentStatus;
import com.fyp.HostelMate.repository.PaymentRepository;
import com.fyp.HostelMate.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment createPayment(Payment payment) {
        payment.setPaymentDate(Instant.now());
        payment.setStatus(PaymentStatus.PENDING);
        // Note: For Khalti integration, additional logic to verify payment with Khalti API would go here.
        return paymentRepository.save(payment);
    }

    @Override
    public Payment updatePaymentStatus(UUID paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(status);
        return paymentRepository.save(payment);
    }

    @Override
    public List<Payment> getPaymentsByStudent(UUID studentId) {
        return paymentRepository.findByStudentStudentId(studentId);
    }

    @Override
    public List<Payment> getPaymentsByHostel(UUID hostelId) {
        return paymentRepository.findByHostelHostelId(hostelId);
    }
}
