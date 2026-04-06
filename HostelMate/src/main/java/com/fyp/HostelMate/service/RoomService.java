package com.fyp.HostelMate.service;

import com.fyp.HostelMate.dto.request.RoomRequest;
import com.fyp.HostelMate.dto.response.RoomResponse;
import com.fyp.HostelMate.entity.User;

import java.util.List;
import java.util.UUID;

public interface RoomService {
    RoomResponse addRoom(User currentUser, RoomRequest request);
    List<RoomResponse> listRooms(User currentUser);
    RoomResponse updateRoom(User currentUser, UUID roomId, RoomRequest request);
    void deleteRoom(User currentUser, UUID roomId);
}
