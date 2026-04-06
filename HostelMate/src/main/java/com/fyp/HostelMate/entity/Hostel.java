package com.fyp.HostelMate.entity;

import com.fyp.HostelMate.entity.enums.HostelType;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "hostels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Hostel {

    @Id
    @GeneratedValue
    @Column(name = "hostel_id")
    private UUID hostelId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String hostelName;

    private String hostelLogo;

    private String ownerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "hostelType_enum")
    private HostelType hostelType;

    private String hostelEmail;

    private String contactNo;

    private String totalRoom;

    @Column(name = "admission_fee")
    private Double admissionFee;

    private Integer establishedYear;

    // Registration / Identity
    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "pan_number")
    private String panNumber;

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

    // Relationships for multiple entities
    @OneToMany(mappedBy = "hostel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HostelPhoto> photos = new ArrayList<>();

    @OneToMany(mappedBy = "hostel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HostelFacility> facilities = new ArrayList<>();

    @OneToMany(mappedBy = "hostel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HostelMealPlan> mealPlans = new ArrayList<>();

    @OneToMany(mappedBy = "hostel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<HostelRule> rules = new ArrayList<>();

    // Helper methods to manage relationships
    public void addPhoto(HostelPhoto photo) {
        photos.add(photo);
        photo.setHostel(this);
    }

    public void removePhoto(HostelPhoto photo) {
        photos.remove(photo);
        photo.setHostel(null);
    }

    public void addFacility(HostelFacility facility) {
        facilities.add(facility);
        facility.setHostel(this);
    }

    public void removeFacility(HostelFacility facility) {
        facilities.remove(facility);
        facility.setHostel(null);
    }

    public void addMealPlan(HostelMealPlan mealPlan) {
        mealPlans.add(mealPlan);
        mealPlan.setHostel(this);
    }

    public void removeMealPlan(HostelMealPlan mealPlan) {
        mealPlans.remove(mealPlan);
        mealPlan.setHostel(null);
    }

    public void addRule(HostelRule rule) {
        rules.add(rule);
        rule.setHostel(this);
    }

    public void removeRule(HostelRule rule) {
        rules.remove(rule);
        rule.setHostel(null);
    }

    private Instant createdAt;




}

