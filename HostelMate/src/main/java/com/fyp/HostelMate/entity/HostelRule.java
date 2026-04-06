package com.fyp.HostelMate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "hostel_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HostelRule {

    @Id
    @GeneratedValue
    @Column(name = "rule_id")
    private UUID ruleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @Column(name = "rule_text", nullable = false, columnDefinition = "TEXT")
    private String ruleText;


}