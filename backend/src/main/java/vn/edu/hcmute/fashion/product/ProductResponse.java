package vn.edu.hcmute.fashion.product;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        long id,
        String name,
        String description,
        long categoryId,
        String categoryName,
        long brandId,
        String brandName,
        String material,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        double averageRating,
        long reviewCount,
        List<ProductImageResponse> images,
        List<ProductVariantResponse> variants) {

    public record ProductImageResponse(long id, String imageUrl, boolean primary) {}
    public record ProductVariantResponse(long id, String size, String color, BigDecimal price, int stockQty) {}
}
