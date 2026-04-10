package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.AddRoomRequest;
import com.fyp.HostelMate.dto.response.ApiResponse;
import com.fyp.HostelMate.service.Impl.RoomServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/hostel/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('HOSTEL')")
public class RoomController {

    private final RoomServiceImpl roomService;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> addRoom(
            Authentication auth,
            @Valid @RequestBody AddRoomRequest req) {
        var room = roomService.addRoom(auth.getName(), req);
        return ResponseEntity.ok(ApiResponse.success("Room added successfully", room));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getMyRooms(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Rooms fetched",
                roomService.getMyRooms(auth.getName())));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Object>> getRoomDetail(
            Authentication auth, @PathVariable UUID roomId) {
        return ResponseEntity.ok(ApiResponse.success("Room detail",
                roomService.getRoomDetail(auth.getName(), roomId)));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(
            Authentication auth, @PathVariable UUID roomId) {
        roomService.deleteRoom(auth.getName(), roomId);
        return ResponseEntity.ok(ApiResponse.success("Room deleted successfully"));
    }
}
