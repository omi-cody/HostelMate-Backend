package com.fyp.HostelMate.dto.request;

import com.fyp.HostelMate.entity.enums.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomRequest {

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    private Integer floor;

    @NotNull(message = "Room type is required")
    private RoomType roomType;
}
