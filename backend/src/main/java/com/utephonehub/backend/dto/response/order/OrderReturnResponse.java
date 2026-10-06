package com.utephonehub.backend.dto.response.order;

import com.utephonehub.backend.entity.OrderReturn;
import com.utephonehub.backend.enums.ReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReturnResponse {
    private Long id;
    private Long orderId;
    private String orderCode;
    private String customerName;
    private String customerEmail;
    private String reason;
    private String evidenceUrl;
    private ReturnStatus status;
    private String adminNote;
    private BigDecimal refundAmount;
    private BigDecimal orderAmount;
    private LocalDateTime orderCreatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public static OrderReturnResponse fromEntity(OrderReturn entity) {
        return OrderReturnResponse.builder()
                .id(entity.getId())
                .orderId(entity.getOrder().getId())
                .orderCode(entity.getOrder().getOrderCode())
                .customerName(entity.getUser().getFullName())
                .customerEmail(entity.getUser().getEmail())
                .reason(entity.getReason())
                .evidenceUrl(entity.getEvidenceUrl())
                .status(entity.getStatus())
                .adminNote(entity.getAdminNote())
                .refundAmount(entity.getRefundAmount())
                .orderAmount(entity.getOrder().getTotalAmount())
                .orderCreatedAt(entity.getOrder().getCreatedAt())
                .createdAt(entity.getCreatedAt())
                .reviewedAt(entity.getReviewedAt())
                .build();
    }
}
