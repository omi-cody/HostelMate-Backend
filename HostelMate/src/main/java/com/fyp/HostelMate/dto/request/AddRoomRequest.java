package com.fyp.HostelMate.dto.request;

import com.fyp.HostelMate.entity.enums.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

// Request to add a room to the hostel.
// Hostel can only do this after their KYC is verified.
@Data
public class AddRoomRequest {

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotNull(message = "Floor number is required")
    private Integer floor;

    @NotNull(message = "Room type is required")
    private RoomType roomType;  // SINGLE, DOUBLE, TRIPLE, QUAD
}
