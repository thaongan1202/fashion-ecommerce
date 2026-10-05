package com.utephonehub.backend.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnStatisticsResponse {
    private long pendingCount;
    private long approvedCount;
    private long rejectedCount;
    private long refundedOrderCount;
    private BigDecimal totalRefundAmount;
    private BigDecimal grossRevenue;
    private BigDecimal revenueAfterRefund;
}
