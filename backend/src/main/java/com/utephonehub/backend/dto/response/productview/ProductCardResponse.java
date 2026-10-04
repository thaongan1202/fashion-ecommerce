package com.utephonehub.backend.dto.response.productview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO cho thẻ sản phẩm hiển thị client.
 * Chỉ chứa các trường thực tế từ Product, ProductTemplate và ProductMetadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCardResponse {
    
    // === BASIC INFO ===
    private Long id;
    private String name;
    private String thumbnailUrl;
    private String brandName;
    private Long brandId;  // For filtering/navigation
    private String categoryName;
    private Long categoryId;  // For filtering/navigation
    
    // === PRICE INFO ===
    private BigDecimal originalPrice;  // Giá gốc (giá thấp nhất)
    private BigDecimal minPrice;  // Giá thấp nhất từ tất cả template
    private BigDecimal maxPrice;  // Giá cao nhất từ tất cả template  
    private String priceRange;  // "10.000.000đ - 15.000.000đ" hoặc "10.000.000đ" nếu chỉ có 1 giá
    private BigDecimal discountedPrice;  // Giá sau giảm
    private Boolean hasDiscount;
    private Double discountPercentage;  // % giảm giá để hiển thị trên UI
    private BigDecimal savingAmount;  // Số tiền tiết kiệm được
    
    // === RATING & REVIEWS ===
    private Double averageRating;
    private Integer totalReviews;
    private String ratingDisplay;  // "4.5 (123 reviews)" for easy display
    
    // === STOCK INFO ===
    private Boolean inStock;
    private Integer stockQuantity;  // Số lượng còn lại
    private String stockStatus;  // "In Stock", "Low Stock", "Out of Stock"
    private Integer soldCount;  // Số lượng đã bán
    
    private String color;
    private String size;
    private String material;
    private String style;
    private String targetAudience;
    private String fit;
    private String season;
    private String pattern;
    private String origin;
}