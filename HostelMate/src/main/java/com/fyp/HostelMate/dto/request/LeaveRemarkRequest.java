package com.fyp.HostelMate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Hostel responds to a student's leave request.
// If rejected, the remark should explain why (e.g. pending dues, notice period).
@Data
public class LeaveRemarkRequest {

    @NotNull(message = "Decision is required")
    private Boolean accept; // true = accept leave, false = reject

    // Required when rejecting - student needs to know what to resolve first
    private String remark;
}
