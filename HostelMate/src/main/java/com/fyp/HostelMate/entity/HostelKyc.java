package com.fyp.HostelMate.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "hostel_kyc")
public class HostelKyc {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "hostel_kyc_id")
    private String hostelKycId;

    @OneToOne
    @JoinColumn(name = "hostel_id", nullable = false, unique = true)
    private Hostel hostel;

    // Registration / Identity
    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "registration_photo_url")
    private String registrationPhotoUrl;

    @Column(name = "id_type")
    private String idType;

    @Column(name = "identity_number")
    private String identityNumber;

    @Column(name = "identity_photo_url")
    private String identityPhotoUrl;

    // Address
    @Column(name = "province")
    private String province;

    @Column(name = "district")
    private String district;

    @Column(name = "municipality")
    private String municipality;

    @Column(name = "tole")
    private String tole;

    @Column(name = "ward_no")
    private String wardNo;

    // Hostel Info
    @Column(name = "hostel_type")
    private String hostelType;

    @Column(name = "admission_fee")
    private Double admissionFee;

    // Lists stored as element collections (separate DB tables)
    @ElementCollection
    @CollectionTable(name = "hostel_kyc_photos", joinColumns = @JoinColumn(name = "hostel_kyc_id"))
    @Column(name = "photo_url")
    private List<String> hostelPhotoUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "hostel_kyc_rules", joinColumns = @JoinColumn(name = "hostel_kyc_id"))
    @Column(name = "rule")
    private List<String> rules = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "hostel_kyc_amenities", joinColumns = @JoinColumn(name = "hostel_kyc_id"))
    @Column(name = "amenity")
    private List<String> amenities = new ArrayList<>();

    // Complex nested structures stored as JSON strings
    @Lob
    @Column(name = "rooms_json", columnDefinition = "TEXT")
    private String roomsJson;

    @Lob
    @Column(name = "meals_json", columnDefinition = "TEXT")
    private String mealsJson;
}
