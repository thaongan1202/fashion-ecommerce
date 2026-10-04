package com.utephonehub.backend.dto.response.review;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReviewOrderOption {
    Long orderId;
    String orderCode;
    String productColor;
    String productSize;
}
