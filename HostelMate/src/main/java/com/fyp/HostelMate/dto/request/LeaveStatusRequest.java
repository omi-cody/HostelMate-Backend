package com.fyp.HostelMate.dto.request;

import com.fyp.HostelMate.entity.enums.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaveStatusRequest {

    @NotNull(message = "status is required (APPROVED or REJECTED)")
    private LeaveStatus status;

    private String hostelRemarks;
}
