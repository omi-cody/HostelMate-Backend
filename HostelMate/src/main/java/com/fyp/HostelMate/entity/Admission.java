package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fyp.HostelMate.entity.enums.AdmissionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// Created when a student officially gets admitted to a hostel.
// This is separate from Application because admission has its own lifecycle:
// active admission -> student requests leave -> hostel accepts -> admission closed.
// Historical admission records are kept even after the student leaves.
@Entity
@Table(name = "admissions")
@Getter
@Setter
public class Admission {

    @Id
    @GeneratedValue
    @Column(name = "admission_id")
    private UUID admissionId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"applications", "payments", "complaintRequests", "notifications", "admissions"})
    private Student student;

    @ManyToOne
    @JoinColumn(name = "hostel_id", nullable = false)
    @JsonIgnoreProperties({"applications", "payments", "events", "reviews", "rooms", "admissions"})
    private Hostel hostel;

    // The room assigned when student got admitted
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = true)  // nullable until hostel allocates room after payment
    @JsonIgnoreProperties({"admissions", "hostel"})
    private Room room;

    // The original application that led to this admission
    @OneToOne
    @JoinColumn(name = "application_id")
    @JsonIgnoreProperties({"student", "hostel"})
    private Application application;

    // ACTIVE means student is currently living there
    // LEAVE_REQUESTED means student clicked the leave button
    // LEFT means hostel accepted the leave
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AdmissionStatus status;

    // The date they were admitted - used to calculate monthly fee due dates
    @Column(name = "admitted_date", nullable = false)
    private LocalDate admittedDate;

    // Monthly fee is due every month on the same day as admittedDate
    @Column(name = "monthly_fee_amount")
    private java.math.BigDecimal monthlyFeeAmount;

    // Set when student requests to leave
    @Column(name = "leave_requested_at")
    private Instant leaveRequestedAt;

    // Set when hostel accepts the leave
    @Column(name = "left_at")
    private Instant leftAt;

    // Hostel's reason if they reject the leave request
    @Column(name = "leave_remark")
    private String leaveRemark;
}
