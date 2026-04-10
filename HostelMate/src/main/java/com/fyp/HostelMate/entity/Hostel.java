package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fyp.HostelMate.entity.enums.HostelType;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Holds basic hostel registration info. Full KYC details go into HostelKyc.
// The hostel becomes visible to students only after admin verifies it AND rooms are added.
@Entity
@Table(name = "hostels")
@Getter
@Setter
public class Hostel {

    @Id
    @GeneratedValue
    @Column(name = "hostel_id")
    private UUID hostelId;

    // Link to the shared users table for login credentials
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonManagedReference
    private User user;

    @Column(name = "hostel_name", nullable = false)
    private String hostelName;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "hostel_type", nullable = false)
    private HostelType hostelType;  // BOYS or GIRLS

    // KYC verification state - pending until admin approves
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // KYC details submitted after registration
    @OneToOne(mappedBy = "hostel", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"hostel", "hostelKyc"})
    private HostelKyc hostelKyc;

    // Rooms added by hostel admin after KYC approval
    @OneToMany(mappedBy = "hostel", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"hostel", "admissions"})
    private List<Room> rooms;

    // All applications received from students
    @OneToMany(mappedBy = "hostel", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"hostel", "student", "applications", "payments", "complaintRequests", "notifications"})
    private List<Application> applications;

    // Events created by this hostel for admitted students
    @OneToMany(mappedBy = "hostel", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"hostel"})
    private List<Event> events;

    // Payments made by students at this hostel (retained even after they leave)
    @OneToMany(mappedBy = "hostel", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"hostel", "student", "admission"})
    private List<Payment> payments;

    // Ratings received from students who left
    @OneToMany(mappedBy = "hostel", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"hostel", "student", "admission"})
    private List<HostelReview> reviews;
}
