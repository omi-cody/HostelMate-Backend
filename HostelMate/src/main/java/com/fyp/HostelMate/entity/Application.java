package com.fyp.HostelMate.entity;

import com.fyp.HostelMate.entity.enums.ApplicationStatus;
import com.fyp.HostelMate.entity.enums.ApplicationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue
    @Column(name = "application_id")
    private UUID applicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_type", nullable = false)
    private ApplicationType applicationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    /** Populated by hostel when scheduling a visit */
    @Column(name = "visit_scheduled_at")
    private LocalDateTime visitScheduledAt;

    /** Hostel's note when approving / scheduling */
    @Column(name = "hostel_remarks", columnDefinition = "TEXT")
    private String hostelRemarks;

    /** Assigned room on admission approval */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @CreationTimestamp
    @Column(name = "applied_at", updatable = false)
    private Instant appliedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
