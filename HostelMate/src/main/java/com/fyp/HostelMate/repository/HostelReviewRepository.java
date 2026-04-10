package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.HostelReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HostelReviewRepository extends JpaRepository<HostelReview, UUID> {

    List<HostelReview> findByHostel_HostelIdOrderByCreatedAtDesc(UUID hostelId);

    // One review per admission - check if student already left a review for this stay
    Optional<HostelReview> findByAdmission_AdmissionId(UUID admissionId);

    // Average rating shown on the hostel's public profile page
    @Query("SELECT AVG(r.rating) FROM HostelReview r WHERE r.hostel.hostelId = :hostelId")
    Double getAverageRatingForHostel(@Param("hostelId") UUID hostelId);
}
