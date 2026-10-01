package vn.edu.hcmute.fashion.catalog.dto;

import vn.edu.hcmute.fashion.catalog.entity.ProductStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public final class CatalogDtos {
    private CatalogDtos() {}

    public record NamedRequest(
            @NotBlank(message = "Tên không được để trống") @Size(max = 100) String name,
            @Size(max = 500) String description) {}

    public record NamedResponse(Long id, String name, String description) {}

    public record ProductRequest(
            @NotBlank(message = "Tên sản phẩm không được để trống") @Size(max = 200) String name,
            String description,
            @NotNull(message = "Thiếu categoryId") Long categoryId,
            @NotNull(message = "Thiếu brandId") Long brandId,
            ProductStatus status) {}

    public record VariantRequest(
            @NotBlank(message = "SKU không được để trống") @Size(max = 64) String sku,
            @Size(max = 50) String size,
            @Size(max = 50) String color,
            @NotNull(message = "Thiếu giá") @DecimalMin(value = "0.0", message = "Giá phải >= 0") BigDecimal price,
            @NotNull(message = "Thiếu tồn kho") @Min(value = 0, message = "Tồn kho phải >= 0") Integer stock) {}

    public record VariantResponse(Long id, String sku, String size, String color,
                                  BigDecimal price, int stock, boolean active) {}

    public record ImageResponse(Long id, String url, int sortOrder) {}

    public record ProductResponse(Long id, String name, String description, ProductStatus status,
                                  NamedResponse category, NamedResponse brand, BigDecimal minPrice,
                                  List<VariantResponse> variants, List<ImageResponse> images) {}
}
