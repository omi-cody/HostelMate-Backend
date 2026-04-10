package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fyp.HostelMate.entity.enums.PaymentMethod;
import com.fyp.HostelMate.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// Tracks each monthly fee payment by a student.
// For Khalti payments: status goes PENDING -> PAID when Khalti confirms.
// For cash payments: hostel manually generates invoice and marks as PAID.
// Payment history is kept permanently even after student leaves the hostel.
@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue
    @Column(name = "payment_id")
    private UUID paymentId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"payments", "applications", "complaintRequests", "notifications"})
    private Student student;

    @ManyToOne
    @JoinColumn(name = "hostel_id", nullable = false)
    @JsonIgnoreProperties({"payments", "applications", "events", "reviews", "rooms"})
    private Hostel hostel;

    @ManyToOne
    @JoinColumn(name = "admission_id")
    @JsonIgnoreProperties({"student", "hostel", "room", "application"})
    private Admission admission;

    // The amount actually charged (monthly fee minus any discount hostel applied)
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    // If hostel gave a discount on this payment, store it for transparency
    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    // What month this payment covers (e.g. 2025-01-01 = January 2025)
    @Column(name = "fee_month", nullable = false)
    private LocalDate feeMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;  // KHALTI or CASH

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;  // PENDING, PAID

    // For Khalti - the transaction ID returned by Khalti API
    @Column(name = "khalti_transaction_id")
    private String khaltiTransactionId;

    // For cash - hostel generates an invoice with a reference number
    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Optional note added by hostel when generating invoice
    @Column(name = "note")
    private String note;
}
