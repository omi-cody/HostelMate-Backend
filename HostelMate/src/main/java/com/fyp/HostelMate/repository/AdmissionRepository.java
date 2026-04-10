package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Admission;
import com.fyp.HostelMate.entity.enums.AdmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdmissionRepository extends JpaRepository<Admission, UUID> {

    // A student can only have one ACTIVE admission at a time (the current hostel they live in)
    Optional<Admission> findByStudent_StudentIdAndStatus(UUID studentId, AdmissionStatus status);

    // All admissions at a hostel with a specific status (e.g. all currently active students)
    List<Admission> findByHostel_HostelIdAndStatusOrderByAdmittedDateDesc(UUID hostelId, AdmissionStatus status);

    // How many students are currently living at this hostel
    long countByHostel_HostelIdAndStatus(UUID hostelId, AdmissionStatus status);

    // Active students in a specific room - used to check occupancy before allowing more admissions
    @Query("SELECT a FROM Admission a WHERE a.room.roomId = :roomId AND a.status = 'ACTIVE'")
    List<Admission> findActiveAdmissionsByRoom(@Param("roomId") UUID roomId);

    // All admissions for a student across all hostels (historical view)
    List<Admission> findByStudent_StudentIdOrderByAdmittedDateDesc(UUID studentId);

    // Monthly fee scheduler needs all active admissions to send reminders
    List<Admission> findByStatus(AdmissionStatus status);

    // Find admission linked to an application (for cancel flow)
    Optional<Admission> findByApplication_ApplicationId(UUID applicationId);


}
