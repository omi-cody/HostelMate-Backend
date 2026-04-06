package com.fyp.HostelMate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentInitiateRequest {

    /** The month this payment covers — format: YYYY-MM-01 */
    @NotNull(message = "paymentForMonth is required (e.g. 2025-05-01)")
    private LocalDate paymentForMonth;
}
