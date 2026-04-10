package com.fyp.HostelMate.dto.request;

import lombok.Data;
import java.util.UUID;

// Hostel sends this when accepting an application.
// For DIRECT_ADMISSION: roomId is null (room allocated after payment).
// For VISIT: roomId still optional at this stage.
@Data
public class AcceptApplicationRequest {
    // Optional - only needed for VISIT type admitAfterVisit
    private UUID roomId;
}
