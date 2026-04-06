package com.fyp.HostelMate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class KhaltiVerifyRequest {

    @NotNull(message = "paymentId is required")
    private UUID paymentId;

    @NotBlank(message = "token is required")
    private String token;

    @NotBlank(message = "khaltiTransactionId is required")
    private String khaltiTransactionId;
}
