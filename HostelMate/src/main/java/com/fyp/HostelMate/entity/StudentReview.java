package com.fyp.HostelMate.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// The hostel gives a rating and review to the student after accepting their leave request.
// This helps other hostels know what kind of tenant the student is.
// Admin and other hostels (once student applies) can see this review.
@Entity
@Table(name = "student_reviews")
@Getter
@Setter
public class StudentReview {

    @Id
    @GeneratedValue
    @Column(name = "review_id")
    private UUID reviewId;

    @ManyToOne
    @JoinColumn(name = "hostel_id", nullable = false)
    @JsonIgnoreProperties({"reviews", "applications", "payments", "events", "rooms", "hostelKyc"})
    private Hostel hostel;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"applications", "payments", "complaintRequests", "notifications", "studentKyc"})
    private Student student;

    @OneToOne
    @JoinColumn(name = "admission_id")
    @JsonIgnoreProperties({"student", "hostel", "room", "application"})
    private Admission admission;

    // Rating out of 5 (1-5)
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
