package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fyp.HostelMate.entity.enums.ApplicationStatus;
import com.fyp.HostelMate.entity.enums.ApplicationType;
import com.fyp.HostelMate.entity.enums.RoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

// Tracks a student's application to a hostel.
// A student can apply to multiple hostels but can only be admitted to one at a time.
// They can still apply elsewhere but must leave their current hostel before getting admitted somewhere new.
@Entity
@Table(name = "applications")
@Getter
@Setter
public class Application {

    @Id
    @GeneratedValue
    @Column(name = "application_id")
    private UUID applicationId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"applications", "payments", "complaintRequests", "notifications"})
    private Student student;

    @ManyToOne
    @JoinColumn(name = "hostel_id", nullable = false)
    @JsonIgnoreProperties({"applications", "payments", "events", "reviews", "rooms", "admissions"})
    private Hostel hostel;

    // The type of room the student wants
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;

    // VISIT means the student wants to see the hostel first, DIRECT means straight admission
    @Enumerated(EnumType.STRING)
    @Column(name = "application_type", nullable = false)
    private ApplicationType applicationType;

    // Application goes through: PENDING -> ACCEPTED or REJECTED
    // For visit: PENDING -> VISIT_SCHEDULED -> ADMITTED or CANCELLED
    // For direct: PENDING -> ACCEPTED -> (awaiting payment) -> ADMITTED
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status;

    // For visit applications - hostel sets a specific date and time for the visit
    @Column(name = "visit_scheduled_at")
    private LocalDateTime visitScheduledAt;

    // Remark from hostel when rejecting or cancelling
    @Column(name = "remark")
    private String remark;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
