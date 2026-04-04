package com.fyp.HostelMate.entity;

import com.fyp.HostelMate.entity.enums.MaintenanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "maintenance_requests")
@Getter
@Setter
public class    MaintenanceRequest {

    @Id
    @GeneratedValue
    @Column(name = "complaint_id")
    private UUID complaintId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_enum")
    private MaintenanceStatus status = MaintenanceStatus.REPORTED;

    @CreationTimestamp
    private Instant createdAt;

    private Instant resolvedAt;
}
