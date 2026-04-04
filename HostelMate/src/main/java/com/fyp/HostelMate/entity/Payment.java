package com.fyp.HostelMate.entity;

import com.fyp.HostelMate.entity.enums.PaymentMethod;
import com.fyp.HostelMate.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue
    @Column(name = "payment_id")
    private UUID paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    private Double amount;

    @CreationTimestamp
    private Instant paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "paymentMethod_enum")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_enum")
    private PaymentStatus status;
}
