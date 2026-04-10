package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.StudentReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentReviewRepository extends JpaRepository<StudentReview, UUID> {

    List<StudentReview> findByStudent_StudentIdOrderByCreatedAtDesc(UUID studentId);

    // One review per admission - hostel can only rate a student once per stay
    Optional<StudentReview> findByAdmission_AdmissionId(UUID admissionId);

    // Average rating helps other hostels assess a student before admitting them
    @Query("SELECT AVG(r.rating) FROM StudentReview r WHERE r.student.studentId = :studentId")
    Double getAverageRatingForStudent(@Param("studentId") UUID studentId);
}
