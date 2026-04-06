package com.fyp.HostelMate.service.Impl;

import com.fyp.HostelMate.dto.request.RoomRequest;
import com.fyp.HostelMate.dto.response.RoomResponse;
import com.fyp.HostelMate.entity.Hostel;
import com.fyp.HostelMate.entity.Room;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.exceptions.BadRequestException;
import com.fyp.HostelMate.exceptions.ResourceNotFoundException;
import com.fyp.HostelMate.repository.HostelRepository;
import com.fyp.HostelMate.repository.RoomRepository;
import com.fyp.HostelMate.service.RoomService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HostelRepository hostelRepository;

    private Hostel getVerifiedHostel(User currentUser) {
        return hostelRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Hostel profile not found."));
    }

    @Override
    @Transactional
    public RoomResponse addRoom(User currentUser, RoomRequest req) {
        Hostel hostel = getVerifiedHostel(currentUser);

        if (roomRepository.existsByHostel_HostelIdAndRoomNumber(hostel.getHostelId(), req.getRoomNumber())) {
            throw new BadRequestException("Room number '" + req.getRoomNumber() + "' already exists in this hostel.");
        }

        Room room = new Room();
        room.setHostel(hostel);
        room.setRoomNumber(req.getRoomNumber());
        room.setFloor(req.getFloor());
        room.setRoomType(req.getRoomType());
        room.setOccupiedCount(0);
        room.setIsActive(true);

        roomRepository.save(room);
        log.info("Room {} added to hostelId={}", req.getRoomNumber(), hostel.getHostelId());
        return RoomResponse.from(room);
    }

    @Override
    public List<RoomResponse> listRooms(User currentUser) {
        Hostel hostel = getVerifiedHostel(currentUser);
        return roomRepository.findByHostel_HostelId(hostel.getHostelId())
                .stream()
                .map(RoomResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(User currentUser, UUID roomId, RoomRequest req) {
        Hostel hostel = getVerifiedHostel(currentUser);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found."));

        if (!room.getHostel().getHostelId().equals(hostel.getHostelId())) {
            throw new BadRequestException("You do not own this room.");
        }

        // Prevent changing room number to one that already exists (unless it's the same room)
        if (!room.getRoomNumber().equals(req.getRoomNumber()) &&
                roomRepository.existsByHostel_HostelIdAndRoomNumber(hostel.getHostelId(), req.getRoomNumber())) {
            throw new BadRequestException("Room number '" + req.getRoomNumber() + "' already exists.");
        }

        // Cannot shrink room type if beds are occupied
        int newCapacity = switch (req.getRoomType()) {
            case SINGLE -> 1; case DOUBLE -> 2; case TRIPLE -> 3; case QUAD -> 4;
        };
        if (room.getOccupiedCount() > newCapacity) {
            throw new BadRequestException("Cannot downgrade room type — " + room.getOccupiedCount() + " beds are currently occupied.");
        }

        room.setRoomNumber(req.getRoomNumber());
        room.setFloor(req.getFloor());
        room.setRoomType(req.getRoomType());

        roomRepository.save(room);
        return RoomResponse.from(room);
    }

    @Override
    @Transactional
    public void deleteRoom(User currentUser, UUID roomId) {
        Hostel hostel = getVerifiedHostel(currentUser);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found."));

        if (!room.getHostel().getHostelId().equals(hostel.getHostelId())) {
            throw new BadRequestException("You do not own this room.");
        }
        if (room.getOccupiedCount() > 0) {
            throw new BadRequestException("Cannot delete a room with active occupants.");
        }

        room.setIsActive(false);
        roomRepository.save(room);
        log.info("Room {} deactivated in hostelId={}", roomId, hostel.getHostelId());
    }
}
