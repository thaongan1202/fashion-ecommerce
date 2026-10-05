package com.utephonehub.backend.dto.request.product;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating/updating ProductTemplate (product variant)
 * SKU, color, size, price, stock
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTemplateRequest {

    /**
     * Stock Keeping Unit - Unique identifier
     * Format: PRODUCT_CODE-COLOR-SIZE (e.g., AT-DEN-M)
     */
    @NotBlank(message = "SKU không được để trống")
    @Pattern(regexp = "^[A-Za-z0-9-_]{3,50}$", message = "SKU phải là chữ cái, số, dấu gạch ngang hoặc gạch dưới (3-50 ký tự)")
    private String sku;

    @Size(max = 50, message = "Màu sắc tối đa 50 ký tự")
    private String color;

    @Size(max = 20, message = "Kích thước tối đa 20 ký tự")
    private String size;

    /**
     * Price for this variant
     */
    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    @Digits(integer = 15, fraction = 2, message = "Giá không hợp lệ")
    private BigDecimal price;

    /**
     * Stock quantity for this variant
     */
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1 và không được âm")
    private Integer stockQuantity;

    /**
     * Template active status
     */
    @Builder.Default
    private Boolean status = true;
}
