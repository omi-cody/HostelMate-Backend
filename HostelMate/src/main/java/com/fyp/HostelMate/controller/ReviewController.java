package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.entity.Review;
import com.fyp.HostelMate.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/add")
    public ResponseEntity<Review> addReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.addReview(review));
    }

    @GetMapping("/hostel/{hostelId}")
    public ResponseEntity<List<Review>> getHostelReviews(@PathVariable UUID hostelId) {
        return ResponseEntity.ok(reviewService.getHostelReviews(hostelId));
    }
}
