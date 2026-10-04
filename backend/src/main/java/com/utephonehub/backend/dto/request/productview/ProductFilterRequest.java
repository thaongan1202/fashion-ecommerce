package com.utephonehub.backend.dto.request.productview;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO cho lọc sản phẩm đa tiêu chí
 * Hỗ trợ kết hợp nhiều filter cùng lúc
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request lọc sản phẩm đa tiêu chí")
public class ProductFilterRequest {
    
    // Danh mục và thương hiệu
    @Schema(description = "Danh sách ID danh mục", example = "[1, 2, 3]")
    private List<Long> categoryIds;
    
    @Schema(description = "Danh sách ID thương hiệu", example = "[1, 2, 3]")
    private List<Long> brandIds;
    
    // Khoảng giá (thanh trượt)
    @Schema(description = "Giá tối thiểu", example = "5000000")
    private BigDecimal minPrice;
    
    @Schema(description = "Giá tối đa", example = "30000000")
    private BigDecimal maxPrice;
    
    @Schema(description = "Danh sách màu sắc", example = "[\"Đen\", \"Trắng\"]")
    private List<String> colorOptions;

    @Schema(description = "Danh sách kích thước", example = "[\"S\", \"M\", \"L\"]")
    private List<String> sizeOptions;

    @Schema(description = "Danh sách chất liệu", example = "[\"Cotton\", \"Denim\"]")
    private List<String> materialOptions;

    @Schema(description = "Danh sách phong cách", example = "[\"Công sở\", \"Đường phố\"]")
    private List<String> styleOptions;

    @Schema(description = "Danh sách đối tượng sử dụng", example = "[\"Nam\", \"Nữ\", \"Unisex\"]")
    private List<String> targetAudienceOptions;
    
    // Đánh giá và trạng thái
    @Schema(description = "Đánh giá tối thiểu (1.0-5.0)", example = "4.0")
    private Double minRating;
    
    @Schema(description = "Đánh giá tối đa (1.0-5.0)", example = "4.5")
    private Double maxRating;
    
    @Schema(description = "Chỉ hiển thị sản phẩm còn hàng", example = "true")
    private Boolean inStockOnly;
    
    @Schema(description = "Chỉ hiển thị sản phẩm có khuyến mãi", example = "true")  
    private Boolean hasDiscountOnly;
    
    // Sorting và Pagination
    @Schema(description = "Sắp xếp theo (name, price, rating, created_date)", example = "price")
    private String sortBy;
    
    @Schema(description = "Hướng sắp xếp (asc, desc)", example = "asc")
    private String sortDirection;
    
    @Schema(description = "Số trang (bắt đầu từ 0)", example = "0")
    @Builder.Default
    private Integer page = 0;
    
    @Schema(description = "Số sản phẩm mỗi trang", example = "20")
    @Builder.Default
    private Integer size = 20;
}