package com.fyp.HostelMate.dto.request;

import com.fyp.HostelMate.entity.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Hostel sends this to update the progress of a complaint or maintenance request.
@Data
public class UpdateComplaintStatusRequest {

    @NotNull(message = "Status is required")
    private RequestStatus status; // PENDING, IN_PROGRESS, or RESOLVED

    // Optional message to the student explaining what was done
    private String hostelResponse;
}
