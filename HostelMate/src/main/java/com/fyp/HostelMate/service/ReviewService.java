package com.fyp.HostelMate.service;

import com.fyp.HostelMate.entity.Review;
import java.util.List;
import java.util.UUID;

public interface ReviewService {
    Review addReview(Review review);
    List<Review> getHostelReviews(UUID hostelId);
}
