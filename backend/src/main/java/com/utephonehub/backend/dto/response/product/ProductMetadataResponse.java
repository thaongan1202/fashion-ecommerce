package com.utephonehub.backend.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for fashion product metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductMetadataResponse {

    private BigDecimal importPrice;
    private BigDecimal salePrice;
    private String material;
    private String style;
    private String targetAudience;
    private String season;
    private String pattern;
    private String fit;
    private String origin;
    private String careInstructions;
    private String additionalSpecs;
}
