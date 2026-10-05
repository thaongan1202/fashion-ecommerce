package com.utephonehub.backend.dto.response.productview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO chi tiết sản phẩm cho client-side
 * Dùng cho trang chi tiết sản phẩm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailViewResponse {
    
    private Long id;
    private String name;
    private String description;
    private String thumbnailUrl;
    
    // Category & Brand
    private CategoryInfo category;
    private BrandInfo brand;
    
    // Images
    private List<ProductImageInfo> images;
    
    // Variants (Templates)
    private List<VariantInfo> variants;
    
    private TechnicalSpecsInfo fashionInfo;
    
    // Rating & Reviews
    private Double averageRating;
    private Integer totalReviews;
    
    // Stock availability
    private Boolean inStock;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandInfo {
        private Long id;
        private String name;
        private String logoUrl;
        private String description;
    }
    
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
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantInfo {
        private Long id;
        private String sku;
        private String color;
        private String size;
        private BigDecimal originalPrice;    // giá gốc từ product_templates.price
        private BigDecimal discountedPrice;  // giá sau khi giảm (có thể null nếu không có khuyến mãi)
        private DiscountInfo discountInfo;   // thông tin chi tiết về giảm giá
        private Integer stockQuantity;  // from product_templates.stock_quantity
        private String stockStatus;     // from product_templates.stock_status
        private Boolean status;         // from product_templates.status
        
        // Tương thích ngược - deprecated, sẽ trả về originalPrice
        @Deprecated
        public BigDecimal getPrice() {
            return originalPrice;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountInfo {
        private BigDecimal discountAmount;     // số tiền đã giảm
        private Double discountPercentage;     // phần trăm giảm giá (đã tính sẵn)
        private String promotionId;            // ID của khuyến mãi được áp dụng
        private String promotionTitle;         // tên khuyến mãi
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnicalSpecsInfo {
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
}
