package com.fyp.HostelMate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wardens")
@Getter
@Setter
public class Warden {

    @Id
    @GeneratedValue
    @Column(name = "warden_id")
    private UUID wardenId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String firstName;
    private String lastName;
    private String contactNumber;
    private String address;

    private Double latitude;
    private Double longitude;

    private String citizenshipFrontImage;
    private String citizenshipBackImage;

    @CreationTimestamp
    private Instant createdAt;
}
