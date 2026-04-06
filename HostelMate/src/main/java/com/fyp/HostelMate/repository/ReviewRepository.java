package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Review;
import com.fyp.HostelMate.entity.enums.ReviewerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    /** All reviews written about a hostel (by students) */
    List<Review> findByHostel_HostelIdAndReviewerType(UUID hostelId, ReviewerType reviewerType);

    /** All reviews written about a student (by hostels) */
    List<Review> findByStudent_StudentIdAndReviewerType(UUID studentId, ReviewerType reviewerType);

    /** Prevent duplicate: one review per admission per reviewer type */
    boolean existsByAdmission_AdmissionIdAndReviewerType(UUID admissionId, ReviewerType reviewerType);

    Optional<Review> findByAdmission_AdmissionIdAndReviewerType(UUID admissionId, ReviewerType reviewerType);

    /** Average rating for a hostel from student reviews */
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.hostel.hostelId = :hostelId AND r.reviewerType = 'STUDENT'")
    Double avgRatingForHostel(UUID hostelId);
}
