package com.fyp.HostelMate.controller;

import com.fyp.HostelMate.dto.request.RoomRequest;
import com.fyp.HostelMate.dto.response.RoomResponse;
import com.fyp.HostelMate.entity.User;
import com.fyp.HostelMate.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/hostel/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /** POST /api/hostel/rooms — add a room */
    @PostMapping
    public ResponseEntity<RoomResponse> addRoom(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.addRoom(currentUser, request));
    }

    /** GET /api/hostel/rooms — list all rooms in this hostel */
    @GetMapping
    public ResponseEntity<List<RoomResponse>> listRooms(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(roomService.listRooms(currentUser));
    }

    /** PUT /api/hostel/rooms/{id} — update a room */
    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody RoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(currentUser, id, request));
    }

    /** DELETE /api/hostel/rooms/{id} — soft-delete (deactivate) a room */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        roomService.deleteRoom(currentUser, id);
        return ResponseEntity.ok(Map.of("message", "Room deactivated successfully."));
    }
}
