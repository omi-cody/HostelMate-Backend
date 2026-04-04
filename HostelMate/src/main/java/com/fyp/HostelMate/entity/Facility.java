package com.fyp.HostelMate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "facilities")
@Getter
@Setter
public class Facility {

    @Id
    @GeneratedValue
    @Column(name = "facility_id")
    private UUID facilityId;

    private String name;
    private String icon;
}
