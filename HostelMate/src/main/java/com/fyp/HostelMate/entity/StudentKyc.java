package com.fyp.HostelMate.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "student_kyc")
public class StudentKyc {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "student_kyc_id")
    private String studentKycId;

    @OneToOne
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "dob")
    private String dob;

    @Column(name = "level_of_study")
    private String levelOfStudy;

    @Column(name = "institute_name")
    private String instituteName;

    @Column(name = "institute_address")
    private String instituteAddress;

    @Column(name = "id_type")
    private String idType;

    @Column(name = "identity_number")
    private String identityNumber;

    @Column(name = "identity_photo_url")
    private String identityPhotoUrl;

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
}
