package com.fyp.HostelMate.dto.request;

import com.fyp.HostelMate.entity.enums.ApplicationType;
import com.fyp.HostelMate.entity.enums.RoomType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

// Student sends this when they want to apply to a hostel.
// The hostelId is in the URL path, so only roomType and applicationType come in the body.
@Data
public class ApplyRequest {

    @NotNull(message = "Room type is required")
    private RoomType roomType;

    @NotNull(message = "Application type is required")
    private ApplicationType applicationType; // VISIT or DIRECT_ADMISSION
}
