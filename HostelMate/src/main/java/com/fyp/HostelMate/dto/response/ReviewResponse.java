package com.fyp.HostelMate.dto.response;

import com.fyp.HostelMate.entity.Review;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID reviewId;
    private String reviewerType;
    private UUID studentId;
    private String studentName;
    private UUID hostelId;
    private String hostelName;
    private UUID admissionId;
    private Integer rating;
    private String comment;
    private Instant createdAt;

    public static ReviewResponse from(Review r) {
        return ReviewResponse.builder()
                .reviewId(r.getReviewId())
                .reviewerType(r.getReviewerType().name())
                .studentId(r.getStudent().getStudentId())
                .studentName(r.getStudent().getFullName())
                .hostelId(r.getHostel().getHostelId())
                .hostelName(r.getHostel().getHostelName())
                .admissionId(r.getAdmission().getAdmissionId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
