package com.fyp.HostelMate.entity;

import com.fyp.HostelMate.entity.enums.AccountStatus;
import com.fyp.HostelMate.entity.enums.UserRole;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
public class User {
    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private UUID userId ;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private UserRole role;

    private String fullName;
    private String email;
    private String phone;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "accountStatus_enum")
    private AccountStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "verificationStatus_enum")
    private VerificationStatus verificationStatus;

    private Instant createdAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Student student;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Hostel hostel;

}
