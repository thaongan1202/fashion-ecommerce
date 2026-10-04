package com.utephonehub.backend.dto.response.productview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO cho hiển thị sản phẩm client-side
 * Dùng cho trang danh sách sản phẩm, tìm kiếm, lọc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductViewResponse {
    
    private Long id;
    private String name;
    private String description;
    private String thumbnailUrl;
    
    // Category & Brand info
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    
    // Price range from templates
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    // Rating & Reviews
    private Double averageRating;
    private Integer totalReviews;
    
    // Stock availability
    private Boolean inStock;
    private Integer totalStock;
    
    // Images
    private List<ProductImageInfo> images;
    
    private String color;
    private String size;
    private String material;
    private String style;
    private String targetAudience;
    private String fit;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImageInfo {
        private Long id;
        private String imageUrl;
        private String altText;
        private Boolean isPrimary;
        private Integer imageOrder;
    }
}
