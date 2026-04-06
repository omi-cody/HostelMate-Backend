package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.ReviewRequest;
import com.fyp.HostelMate.dto.response.ReviewResponse;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.service.Impl.ReviewServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewServiceImpl reviewService;

    /** POST /api/student/reviews — student reviews a hostel after leaving */
    @PostMapping("/api/student/reviews")
    public ResponseEntity<ReviewResponse> studentReview(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.studentReviewHostel(currentUser, request));
    }

    /** POST /api/hostel/reviews — hostel reviews a student after they leave */
    @PostMapping("/api/hostel/reviews")
    public ResponseEntity<ReviewResponse> hostelReview(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.hostelReviewStudent(currentUser, request));
    }

    /** GET /api/hostels/{id}/reviews — public: get all student reviews for a hostel */
    @GetMapping("/api/hostels/{id}/reviews")
    public ResponseEntity<List<ReviewResponse>> getHostelReviews(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.getHostelReviews(id));
    }

    /** GET /api/hostel/students/{studentId}/reviews — hostel views a student's past reviews */
    @GetMapping("/api/hostel/students/{studentId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getStudentReviews(@PathVariable UUID studentId) {
        return ResponseEntity.ok(reviewService.getStudentReviews(studentId));
    }

    /** GET /api/admin/reviews — admin sees all reviews */
    @GetMapping("/api/admin/reviews")
    public ResponseEntity<List<ReviewResponse>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }
}
