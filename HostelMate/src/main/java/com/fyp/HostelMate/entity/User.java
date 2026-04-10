package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fyp.HostelMate.entity.enums.AccountStatus;
import com.fyp.HostelMate.entity.enums.UserRole;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// This is the central user table shared by students, hostels, and admin.
// We keep role-specific data in separate tables (Student, Hostel) and link via FK.
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole role;

    // Full name is used for both student name and hostel owner name
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String passwordHash;

    // ACTIVE means account is usable, BLOCKED means admin disabled it
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus status;

    // KYC verification state - users can only use system features after VERIFIED
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Each user is either a student or a hostel, not both.
    // CascadeType.ALL so if we delete a user, the linked student/hostel goes too.
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"user", "applications", "payments", "complaintRequests", "notifications"})
    private Student student;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"user", "applications", "payments", "events", "reviews", "rooms", "hostelKyc"})
    private Hostel hostel;
}
