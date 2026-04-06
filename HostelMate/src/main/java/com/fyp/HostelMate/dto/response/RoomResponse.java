package com.fyp.HostelMate.dto.response;

import com.fyp.HostelMate.entity.Room;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RoomResponse {
    private UUID roomId;
    private UUID hostelId;
    private String roomNumber;
    private Integer floor;
    private String roomType;
    private Integer occupiedCount;
    private Integer availableBeds;
    private Integer totalCapacity;
    private Boolean isActive;

    public static RoomResponse from(Room r) {
        int capacity = switch (r.getRoomType()) {
            case SINGLE -> 1;
            case DOUBLE -> 2;
            case TRIPLE -> 3;
            case QUAD   -> 4;
        };
        return RoomResponse.builder()
                .roomId(r.getRoomId())
                .hostelId(r.getHostel().getHostelId())
                .roomNumber(r.getRoomNumber())
                .floor(r.getFloor())
                .roomType(r.getRoomType().name())
                .occupiedCount(r.getOccupiedCount())
                .availableBeds(r.getAvailableBeds())
                .totalCapacity(capacity)
                .isActive(r.getIsActive())
                .build();
    }
}
