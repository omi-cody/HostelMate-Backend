package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fyp.HostelMate.entity.enums.ComplaintType;
import com.fyp.HostelMate.entity.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// Handles both complaints and maintenance requests from admitted students.
// The student selects a type (COMPLAINT or MAINTENANCE), gives a name and description.
// Hostel sees the request and updates the status as they work on it.
// These are cleared when a student leaves the hostel.
@Entity
@Table(name = "complaint_requests")
@Getter
@Setter
public class ComplaintRequest {

    @Id
    @GeneratedValue
    @Column(name = "request_id")
    private UUID requestId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"complaintRequests", "applications", "payments", "notifications"})
    private Student student;

    @ManyToOne
    @JoinColumn(name = "hostel_id", nullable = false)
    @JsonIgnoreProperties({"applications", "payments", "events", "reviews", "rooms"})
    private Hostel hostel;

    // COMPLAINT for general issues, MAINTENANCE for repair/fix requests
    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private ComplaintType requestType;

    // Short title like "Broken fan in room 101"
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    // PENDING -> IN_PROGRESS -> RESOLVED (hostel updates this)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status;

    // Optional note from hostel when they update the status
    @Column(name = "hostel_response")
    private String hostelResponse;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
