package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fyp.HostelMate.entity.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// In-app notifications sent to students and hostels.
// When a student leaves a hostel, their notifications are cleared (isDeleted = true).
// Notifications are created by the system automatically on key events:
// application status change, KYC status change, event added, fee reminder, etc.
@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue
    @Column(name = "notification_id")
    private UUID notificationId;

    // Who this notification belongs to
    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"notifications", "applications", "payments", "complaintRequests"})
    private Student student;

    // Hostel notifications also go through this table, linked by hostelId
    @ManyToOne
    @JoinColumn(name = "hostel_id")
    @JsonIgnoreProperties({"applications", "payments", "events", "reviews", "rooms"})
    private Hostel hostel;

    // SHORT label to categorize what kind of event triggered this notification
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    // The actual message shown to the user
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    // False until the user opens/reads the notification
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    // When a student leaves, we soft-delete their notifications rather than hard delete
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Optional link to the entity that triggered this (e.g. applicationId, eventId)
    @Column(name = "reference_id")
    private String referenceId;
}
