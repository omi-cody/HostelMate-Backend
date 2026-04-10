package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.AddRoomRequest;
import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.Room;
import com.fyp.HostelMate.entity.enums.AdmissionStatus;
import com.fyp.HostelMate.entity.enums.RoomType;
import com.fyp.HostelMate.entity.enums.VerificationStatus;
import com.fyp.HostelMate.exceptions.BusinessException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.AdmissionRepository;
import com.fyp.HostelMate.repository.HostelRepository;
import com.fyp.HostelMate.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl {

    private final RoomRepository roomRepo;
    private final HostelRepository hostelRepo;
    private final AdmissionRepository admissionRepo;

    // ADD ROOM
    @Transactional
    public Room addRoom(String email, AddRoomRequest req) {

        Hostel hostel = getVerifiedHostel(email);

        // Don't allow duplicate room numbers within the same hostel
        if (roomRepo.existsByHostel_HostelIdAndRoomNumber(hostel.getHostelId(), req.getRoomNumber()))
            throw new BusinessException("Room number '" + req.getRoomNumber() +
                    "' already exists in this hostel");

        Room room = new Room();
        room.setHostel(hostel);
        room.setRoomNumber(req.getRoomNumber());
        room.setFloor(req.getFloor());
        room.setRoomType(req.getRoomType());
        // Set capacity based on room type (SINGLE=1, DOUBLE=2, TRIPLE=3, QUAD=4)
        room.setCapacity(getCapacityForType(req.getRoomType()));

        Room saved = roomRepo.save(room);
        log.info("Room {} added to hostel {}", req.getRoomNumber(), hostel.getHostelName());
        return saved;
    }

    //GET ALL ROOMS
    public List<Room> getMyRooms(String email) {
        Hostel hostel = getHostelByEmail(email);
        return roomRepo.findByHostel_HostelId(hostel.getHostelId());
    }

    // GET ROOM DETAIL with occupancy
    public Map<String, Object> getRoomDetail(String email, UUID roomId) {
        Hostel hostel = getHostelByEmail(email);
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getHostel().getHostelId().equals(hostel.getHostelId()))
            throw new BusinessException("This room does not belong to your hostel");

        List<?> activeAdmissions = admissionRepo.findActiveAdmissionsByRoom(roomId);
        int occupied = activeAdmissions.size();

        return Map.of(
                "room", room,
                "occupied", occupied,
                "available", room.getCapacity() - occupied,
                "admissions", activeAdmissions
        );
    }

    //  DELETE ROOM
    @Transactional
    public void deleteRoom(String email, UUID roomId) {

        Hostel hostel = getHostelByEmail(email);
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getHostel().getHostelId().equals(hostel.getHostelId()))
            throw new BusinessException("This room does not belong to your hostel");

        // Do not delete a room that currently has students in it
        List<?> activeAdmissions = admissionRepo.findActiveAdmissionsByRoom(roomId);
        if (!activeAdmissions.isEmpty())
            throw new BusinessException(
                    "Cannot delete room - it currently has " + activeAdmissions.size() +
                    " active student(s). Relocate them first.");

        roomRepo.delete(room);
        log.info("Room {} deleted from hostel {}", room.getRoomNumber(), hostel.getHostelName());
    }

    // HELPERS

    private Hostel getVerifiedHostel(String email) {
        Hostel hostel = getHostelByEmail(email);
        if (hostel.getVerificationStatus() != VerificationStatus.VERIFIED)
            throw new BusinessException(
                    "Your hostel KYC must be verified before adding rooms.");
        return hostel;
    }

    private Hostel getHostelByEmail(String email) {
        return hostelRepo.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found"));
    }

    private int getCapacityForType(RoomType type) {
        return switch (type) {
            case SINGLE -> 1;
            case DOUBLE -> 2;
            case TRIPLE -> 3;
            case QUAD   -> 4;
        };
    }
}
