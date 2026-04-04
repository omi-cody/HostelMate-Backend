package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.entity.Review;
import com.fyp.HostelMate.repository.ReviewRepository;
import com.fyp.HostelMate.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Review addReview(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getHostelReviews(UUID hostelId) {
        return reviewRepository.findByHostelHostelId(hostelId);
    }
}
