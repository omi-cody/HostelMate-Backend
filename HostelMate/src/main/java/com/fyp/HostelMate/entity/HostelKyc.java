package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Stores all the KYC details for a hostel that admin needs to verify.
// After verification, the hostel becomes searchable by students.
// Fields that cannot be changed after approval: panNumber, panDocumentUrl.
@Entity
@Table(name = "hostel_kyc")
@Getter
@Setter
public class HostelKyc {

    @Id
    @GeneratedValue
    @Column(name = "kyc_id")
    private UUID kycId;

    @OneToOne
    @JoinColumn(name = "hostel_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hostelKyc", "rooms", "applications", "events", "payments", "reviews", "user"})
    private Hostel hostel;

    // --- Basic hostel info ---
    @Column(name = "logo_url")
    private String logoUrl;

    // Admission fee is the one-time fee students pay when directly admitted
    @Column(name = "admission_fee", nullable = false)
    private BigDecimal admissionFee;

    @Column(name = "established_year", nullable = false)
    private Integer establishedYear;

    // --- Address ---
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

    // --- PAN document - cannot be changed after admin verifies ---
    @Column(name = "pan_number", nullable = false, unique = true)
    private String panNumber;

    @Column(name = "pan_document_url", nullable = false)
    private String panDocumentUrl;

    // Up to 4 hostel photos stored as comma-separated URLs or a JSON array
    // Using a simple text column; frontend will parse it as an array
    @Column(name = "hostel_photo_urls", columnDefinition = "TEXT")
    private String hostelPhotoUrls;

    // --- Room pricing (stored as embedded JSON or separate table) ---
    // Using separate RoomPricing entities for clean querying and future updates
    @OneToMany(mappedBy = "hostelKyc", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"hostelKyc"})
    private List<RoomPricing> roomPricings;

    // --- Amenities list - stored as TEXT, comma-separated for simplicity ---
    @Column(columnDefinition = "TEXT")
    private String amenities;

    // --- Rules and regulations ---
    @Column(name = "rules_and_regulations", columnDefinition = "TEXT")
    private String rulesAndRegulations;

    // Meal plan stored as separate entities for easy weekly view
    @OneToMany(mappedBy = "hostelKyc", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"hostelKyc"})
    private List<MealPlan> mealPlans;

    // --- Admin verification ---
    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false)
    private VerificationStatus kycStatus;

    @Column(name = "rejection_remark")
    private String rejectionRemark;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;
}
