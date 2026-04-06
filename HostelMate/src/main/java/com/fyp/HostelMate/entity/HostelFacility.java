package com.fyp.HostelMate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "hostel_facilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HostelFacility {

    @Id
    @GeneratedValue
    @Column(name = "facility_id")
    private UUID facilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @Column(name = "facility_name", nullable = false)
    private String facilityName;


}