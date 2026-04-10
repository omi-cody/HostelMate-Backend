package com.fyp.HostelMate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

// Student initiates a Khalti payment for their monthly fee.
// This creates a PENDING payment record and returns the Khalti payment URL.
@Data
public class KhaltiPaymentRequest {

    @NotNull(message = "Admission ID is required")
    private UUID admissionId;

    @NotNull(message = "Fee month is required")
    private LocalDate feeMonth; // e.g. 2025-03-01 for March 2025
}
