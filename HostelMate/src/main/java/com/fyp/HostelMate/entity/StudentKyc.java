package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fyp.HostelMate.entity.enums.DietType;
import com.fyp.HostelMate.entity.enums.DocumentType;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// This table stores all the extra info admin needs to verify a student.
// The student fills this form after the initial registration step.
// Admin can only approve or reject after all fields are present.
@Entity
@Table(name = "student_kyc")
@Getter
@Setter
public class StudentKyc {

    @Id
    @GeneratedValue
    @Column(name = "kyc_id")
    private UUID kycId;

    // One-to-one: each student has exactly one KYC record
    @OneToOne
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"studentKyc", "applications", "payments", "complaintRequests", "notifications"})
    private Student student;

    // --- Personal details ---
    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    // DOB stored as LocalDate so we can calculate age if needed
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "diet_type", nullable = false)
    private DietType dietType;  // VEG or NON_VEG

    // --- Guardian details ---
    @Column(name = "guardian_name", nullable = false)
    private String guardianName;

    @Column(name = "guardian_relation", nullable = false)
    private String guardianRelation;

    @Column(name = "guardian_phone", nullable = false)
    private String guardianPhone;

    // --- Document details ---
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;  // CITIZENSHIP, VOTER_ID, NATIONAL_ID

    @Column(name = "identity_number", nullable = false)
    private String identityNumber;

    @Column(name = "document_photo_url", nullable = false)
    private String documentPhotoUrl;

    // --- Institute details ---
    @Column(name = "institute_name", nullable = false)
    private String instituteName;

    @Column(name = "institute_address", nullable = false)
    private String instituteAddress;

    @Column(name = "level_of_study", nullable = false)
    private String levelOfStudy;  // e.g. Bachelor, Master, Diploma, +2

    // --- Permanent address ---
    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String municipality;

    @Column(nullable = false)
    private String tole;

    @Column(name = "ward_number", nullable = false)
    private String wardNumber;

    // --- Admin verification fields ---
    // Status here mirrors the user's verificationStatus but keeps rejection remarks
    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false)
    private VerificationStatus kycStatus;

    // Admin fills this when rejecting - tells student what to fix
    @Column(name = "rejection_remark")
    private String rejectionRemark;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;
}
