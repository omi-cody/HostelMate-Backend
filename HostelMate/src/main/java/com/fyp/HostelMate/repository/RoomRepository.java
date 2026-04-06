package com.fyp.HostelMate.repository;

import com.fyp.HostelMate.entity.Room;
import com.fyp.HostelMate.entity.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByHostel_HostelId(UUID hostelId);

    List<Room> findByHostel_HostelIdAndIsActiveTrue(UUID hostelId);

    List<Room> findByHostel_HostelIdAndRoomType(UUID hostelId, RoomType roomType);

    boolean existsByHostel_HostelIdAndRoomNumber(UUID hostelId, String roomNumber);
}
