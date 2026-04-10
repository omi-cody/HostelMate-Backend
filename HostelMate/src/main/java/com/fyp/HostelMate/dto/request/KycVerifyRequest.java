package com.fyp.HostelMate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Admin sends this when reviewing a student or hostel KYC submission.
// If action is REJECTED, remark is required so the user knows what to fix.
@Data
public class KycVerifyRequest {

    @NotNull(message = "Action is required")
    private String action; // "VERIFIED" or "REJECTED"

    // Required only when rejecting - tells the user what to correct and resubmit
    private String remark;
}
