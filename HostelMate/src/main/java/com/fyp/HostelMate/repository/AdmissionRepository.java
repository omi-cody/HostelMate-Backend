package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Admission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdmissionRepository extends JpaRepository<Admission, UUID> {

    Optional<Admission> findByStudent_StudentIdAndIsActiveTrue(UUID studentId);

    List<Admission> findByHostel_HostelIdAndIsActiveTrue(UUID hostelId);

    boolean existsByStudent_StudentIdAndIsActiveTrue(UUID studentId);

    @Query("SELECT COUNT(a) FROM Admission a WHERE a.hostel.hostelId = :hostelId AND a.isActive = true AND a.mealPreference = 'VEG'")
    long countVegByHostelId(UUID hostelId);

    @Query("SELECT COUNT(a) FROM Admission a WHERE a.hostel.hostelId = :hostelId AND a.isActive = true AND a.mealPreference = 'NON_VEG'")
    long countNonVegByHostelId(UUID hostelId);
}
