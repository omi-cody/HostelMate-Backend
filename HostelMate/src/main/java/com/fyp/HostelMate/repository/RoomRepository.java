package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Room;
import com.fyp.HostelMate.entity.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByHostel_HostelId(UUID hostelId);

    // Prevent two rooms with the same number in the same hostel
    boolean existsByHostel_HostelIdAndRoomNumber(UUID hostelId, String roomNumber);

    // Available rooms = rooms where current active admissions count is less than capacity
    @Query("SELECT r FROM Room r WHERE r.hostel.hostelId = :hostelId " +
           "AND r.roomType = :roomType " +
           "AND (SELECT COUNT(a) FROM Admission a WHERE a.room = r AND a.status = 'ACTIVE') < r.capacity")
    List<Room> findAvailableRoomsByType(@Param("hostelId") UUID hostelId,
                                        @Param("roomType") RoomType roomType);

    // Count how many beds are currently occupied across all rooms in a hostel
    @Query("SELECT COALESCE(SUM(r.capacity), 0) FROM Room r WHERE r.hostel.hostelId = :hostelId")
    int getTotalCapacity(@Param("hostelId") UUID hostelId);
}
