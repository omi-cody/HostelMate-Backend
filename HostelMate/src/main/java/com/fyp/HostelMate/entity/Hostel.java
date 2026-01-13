package com.fyp.HostelMate.entity;

import com.fyp.HostelMate.entity.enums.HostelType;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hostels")
@Getter
@Setter
public class Hostel {

    @Id
    @GeneratedValue
    @Column(name = "hostel_id")
    private UUID hostelId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String hostelName;

    private String ownerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "hostelType_enum")
    private HostelType hostelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "verificationStatus_enum")
    private VerificationStatus verificationStatus;

    private Instant createdAt;
}

