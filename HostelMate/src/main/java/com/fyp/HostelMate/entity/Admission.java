package com.fyp.HostelMate.entity;

import com.fyp.HostelMate.entity.enums.MealPreference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents an active (or past) admission of a student to a hostel.
 * A student can only have ONE active admission at a time.
 */
@Entity
@Table(name = "admissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Admission {

    @Id
    @GeneratedValue
    @Column(name = "admission_id")
    private UUID admissionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @Column(name = "monthly_fee", nullable = false)
    private Double monthlyFee;

    /** Date the next payment is due */
    @Column(name = "next_payment_due")
    private LocalDate nextPaymentDue;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_preference")
    private MealPreference mealPreference = MealPreference.NON_VEG;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /** Populated when student leaves */
    @Column(name = "leave_date")
    private LocalDate leaveDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
