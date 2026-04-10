package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.enums.HostelType;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HostelRepository extends JpaRepository<Hostel, UUID> {

    Optional<Hostel> findByUser_UserId(UUID userId);

    Optional<Hostel> findByUser_Email(String email);

    List<Hostel> findByVerificationStatus(VerificationStatus status);

    // ── PUBLIC SEARCH ─────────────────────────────────────────────────────────
    // Hibernate 7 + PostgreSQL cannot resolve the type of a null enum parameter
    // in ":param IS NULL OR col = :param" expressions — it passes the null as
    // bytea and then lower(bytea) fails.  The fix is to provide four concrete
    // queries (name+type / name-only / type-only / no-filter) so no parameter
    // is ever null.  The service layer picks the right variant.

    // No filters - return all verified hostels that have at least one room
    @Query("SELECT h FROM Hostel h WHERE h.verificationStatus = 'VERIFIED' AND SIZE(h.rooms) > 0")
    List<Hostel> findAllVerifiedWithRooms();

    // Filter by name only (case-insensitive)
    @Query("SELECT h FROM Hostel h WHERE h.verificationStatus = 'VERIFIED' AND SIZE(h.rooms) > 0 " +
           "AND LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Hostel> findVerifiedByName(@Param("name") String name);

    // Filter by hostel type only (BOYS / GIRLS)
    @Query("SELECT h FROM Hostel h WHERE h.verificationStatus = 'VERIFIED' AND SIZE(h.rooms) > 0 " +
           "AND h.hostelType = :hostelType")
    List<Hostel> findVerifiedByType(@Param("hostelType") HostelType hostelType);

    // Filter by both name and hostel type
    @Query("SELECT h FROM Hostel h WHERE h.verificationStatus = 'VERIFIED' AND SIZE(h.rooms) > 0 " +
           "AND LOWER(h.hostelName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "AND h.hostelType = :hostelType")
    List<Hostel> findVerifiedByNameAndType(@Param("name") String name,
                                           @Param("hostelType") HostelType hostelType);

    // Keep the old method signature so existing code still compiles.
    // Delegates to the correct no-null variant based on which params are present.
    default List<Hostel> searchVerifiedHostels(String name, HostelType hostelType) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasType = hostelType != null;
        if (hasName && hasType)  return findVerifiedByNameAndType(name, hostelType);
        if (hasName)             return findVerifiedByName(name);
        if (hasType)             return findVerifiedByType(hostelType);
        return findAllVerifiedWithRooms();
    }
}
