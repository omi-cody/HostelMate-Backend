package com.fyp.HostelMate.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// Hostel fills this when a student pays in cash.
// The hostel can optionally give a discount before generating the invoice.
@Data
public class GenerateInvoiceRequest {

    @NotNull(message = "Student admission ID is required")
    private java.util.UUID admissionId;

    @NotNull(message = "Fee month is required")
    private LocalDate feeMonth; // First day of the month being paid for

    // Optional discount amount in NPR - deducted from monthly fee
    private BigDecimal discountAmount;

    // Optional internal note (e.g. "Discount for early payment")
    private String note;
}
