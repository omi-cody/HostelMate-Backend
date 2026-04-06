package com.fyp.HostelMate.entity;

import com.fyp.HostelMate.entity.enums.GenderType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "students")
@Getter
@Setter
public class Student {
    @Id
    @GeneratedValue
    @Column(name = "student_id")
    private UUID studentId ;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_enum", nullable = false)
    private GenderType gender;

    private String profilePicture;

    private String fullName;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    private String email;
    private String instituteName;
    private String levelOfStudy;
    private String instituteAddress;
    private String documentType;
    private String documentNumber;

    private String documentPhoto;

    private String province;
    private String district;
    private String municipality;
    private String tole;
    private int wardNumber;

    @CreationTimestamp
    private Instant createdAt;



}
