package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fyp.HostelMate.entity.enums.GenderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Holds student-specific info that doesn't belong in the shared users table.
// Profile details like gender live here; KYC details are in StudentKyc.
@Entity
@Table(name = "students")
@Getter
@Setter
public class Student {

    @Id
    @GeneratedValue
    @Column(name = "student_id")
    private UUID studentId;

    // Every student links back to a user account for login credentials
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"student", "hostel", "passwordHash"})
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private GenderType gender;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // KYC is filled separately after registration - it's a one-to-one optional until submitted
    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"student"})
    private StudentKyc studentKyc;

    // A student can have multiple hostel applications over time
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"student", "hostel", "applications", "rooms", "events", "payments"})
    private List<Application> applications;

    // Fee payments made by this student (kept even after leaving a hostel)
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"student", "hostel", "admission"})
    private List<Payment> payments;

    // Complaints and maintenance requests sent to their hostel
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"student", "hostel"})
    private List<ComplaintRequest> complaintRequests;

    // Notifications sent to this student (cleared when they leave a hostel)
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"student", "hostel"})
    private List<Notification> notifications;
}
