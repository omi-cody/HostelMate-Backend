package com.fyp.HostelMate.entity;

import com.fyp.HostelMate.enums.*;
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

    private Instant createdAt;

}
