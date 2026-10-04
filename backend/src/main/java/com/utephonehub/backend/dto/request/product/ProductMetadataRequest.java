package com.utephonehub.backend.dto.request.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating/updating fashion product metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductMetadataRequest {

    @DecimalMin(value = "0.0", message = "Giá nhập phải >= 0")
    private BigDecimal importPrice;

    @DecimalMin(value = "0.0", message = "Giá niêm yết phải >= 0")
    private BigDecimal salePrice;

    @Size(max = 100, message = "Chất liệu tối đa 100 ký tự")
    private String material;

    @Size(max = 100, message = "Phong cách tối đa 100 ký tự")
    private String style;

    @Size(max = 50, message = "Đối tượng sử dụng tối đa 50 ký tự")
    private String targetAudience;

    @Size(max = 50, message = "Mùa tối đa 50 ký tự")
    private String season;

    @Size(max = 100, message = "Họa tiết tối đa 100 ký tự")
    private String pattern;

    @Size(max = 100, message = "Kiểu dáng tối đa 100 ký tự")
    private String fit;

    @Size(max = 100, message = "Xuất xứ tối đa 100 ký tự")
    private String origin;

    @Size(max = 2000, message = "Hướng dẫn bảo quản tối đa 2000 ký tự")
    private String careInstructions;

    @Size(max = 2000, message = "Thông tin bổ sung tối đa 2000 ký tự")
    private String additionalSpecs;
}
