package com.utephonehub.backend.dto.response.productview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO cho so sánh sản phẩm
 * Cho phép so sánh tối đa 4 sản phẩm cùng lúc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductComparisonResponse {
    
    private List<ComparisonProduct> products;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonProduct {
        
        // Basic Info
        private Long id;
        private String name;
        private String thumbnailUrl;
        private String brandName;
        
        // Price (single template = single price)
        private BigDecimal originalPrice;
        private BigDecimal discountedPrice;
        private Boolean hasDiscount;
        
        // Rating
        private Double averageRating;
        private Integer totalReviews;
        
        // Stock
        private Boolean inStock;
        
        private ComparisonSpecs specs;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonSpecs {
        private String material;
        private String style;
        private String targetAudience;
        private String fit;
        private String season;
        private String pattern;
        private String origin;
        private String colors;
        private String sizes;
        private String careInstructions;
    }
}
