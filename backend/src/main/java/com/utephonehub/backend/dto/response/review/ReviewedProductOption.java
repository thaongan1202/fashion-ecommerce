package com.utephonehub.backend.dto.response.review;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReviewedProductOption {
    Long id;
    String name;
    Long reviewCount;
}
