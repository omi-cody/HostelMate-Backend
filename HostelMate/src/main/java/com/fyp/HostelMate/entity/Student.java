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

    private String fullName;
    private String contactNumber;
    private String parentGuardianContact;
    private LocalDate dateOfBirth;
    private String address;

    @CreationTimestamp
    private Instant createdAt;

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL)
    private StudentKyc studentKyc;

}
