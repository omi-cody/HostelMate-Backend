package com.fyp.HostelMate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CashPaymentRequest {

    @NotNull(message = "studentId is required")
    private UUID studentId;

    @NotNull(message = "paymentForMonth is required (e.g. 2025-05-01)")
    private LocalDate paymentForMonth;

    /** Optional override — defaults to student's monthly fee */
    private Double amount;
}
