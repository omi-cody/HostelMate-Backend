package com.fyp.HostelMate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "rules")
@Getter
@Setter
public class Rule {

    @Id
    @GeneratedValue
    @Column(name = "rule_id")
    private UUID ruleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    private String ruleDescription;
}
