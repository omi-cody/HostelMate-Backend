package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// A student submits this review after their leave is accepted by the hostel.
// It's mandatory - student must rate and give feedback before they can move on.
// This is publicly visible on the hostel's profile page.
@Entity
@Table(name = "hostel_reviews")
@Getter
@Setter
public class HostelReview {

    @Id
    @GeneratedValue
    @Column(name = "review_id")
    private UUID reviewId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"reviews", "applications", "payments", "complaintRequests", "notifications"})
    private Student student;

    @ManyToOne
    @JoinColumn(name = "hostel_id", nullable = false)
    @JsonIgnoreProperties({"reviews", "applications", "payments", "events", "rooms"})
    private Hostel hostel;

    // Which admission stay this review is for
    @OneToOne
    @JoinColumn(name = "admission_id")
    @JsonIgnoreProperties({"student", "hostel", "room", "application"})
    private Admission admission;

    // Rating out of 5 (1-5 stars)
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
