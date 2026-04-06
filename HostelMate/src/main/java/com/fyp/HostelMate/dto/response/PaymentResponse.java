package com.fyp.HostelMate.dto.response;

import com.fyp.HostelMate.entity.Payment;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID paymentId;
    private UUID studentId;
    private String studentName;
    private UUID hostelId;
    private String hostelName;
    private Double amount;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDate paymentForMonth;
    private String transactionToken;
    private String khaltiTransactionId;
    private String invoiceNumber;
    private Instant createdAt;
    private Instant paidAt;

    public static PaymentResponse from(Payment p) {
        return PaymentResponse.builder()
                .paymentId(p.getPaymentId())
                .studentId(p.getStudent().getStudentId())
                .studentName(p.getStudent().getFullName())
                .hostelId(p.getHostel().getHostelId())
                .hostelName(p.getHostel().getHostelName())
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod().name())
                .paymentStatus(p.getPaymentStatus().name())
                .paymentForMonth(p.getPaymentForMonth())
                .transactionToken(p.getTransactionToken())
                .khaltiTransactionId(p.getKhaltiTransactionId())
                .invoiceNumber(p.getInvoiceNumber())
                .createdAt(p.getCreatedAt())
                .paidAt(p.getPaidAt())
                .build();
    }
}
