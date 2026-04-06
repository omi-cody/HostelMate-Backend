package com.fyp.HostelMate.dto.request;

import com.fyp.HostelMate.entity.enums.ApplicationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ApplicationStatusRequest {

    /** APPROVED, REJECTED, or CANCELLED */
    private ApplicationStatus status;

    /** Required when status = APPROVED and applicationType = VISIT */
    private LocalDateTime visitScheduledAt;

    /** Required when status = APPROVED and applicationType = ADMISSION */
    private UUID roomId;

    /** Optional note from hostel */
    private String hostelRemarks;
}
