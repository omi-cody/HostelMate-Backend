package com.fyp.HostelMate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaymentSummaryResponse {
    private Double totalPaid;
    private Double dueAmount;
    private List<PaymentResponse> payments;
}
