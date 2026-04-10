package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

// Hostel creates events that all admitted students can see.
// When an event is created, a notification is sent to every student currently admitted to that hostel.
// Email is also sent for events.
@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue
    @Column(name = "event_id")
    private UUID eventId;

    @ManyToOne
    @JoinColumn(name = "hostel_id", nullable = false)
    @JsonIgnoreProperties({"events", "applications", "payments", "reviews", "rooms", "hostelKyc"})
    private Hostel hostel;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    // Where the event will happen - could be a room, hall, or external location
    @Column(name = "location", nullable = false)
    private String location;

    // When the event is scheduled to happen
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // If the hostel edits or deletes the event, we track when it was last changed
    @Column(name = "updated_at")
    private Instant updatedAt;
}
