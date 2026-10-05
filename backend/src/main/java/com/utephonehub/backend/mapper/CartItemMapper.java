package com.utephonehub.backend.mapper;

import com.utephonehub.backend.dto.response.cart.CartItemResponse;
import com.utephonehub.backend.entity.CartItem;
import com.utephonehub.backend.entity.ProductTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

/**
 * Mapper for CartItem entity to CartItemResponse DTO
 * Updated to calculate price and stock from ProductTemplate entities
 */
@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productThumbnailUrl", source = "product.thumbnailUrl")
    @Mapping(target = "unitPrice", expression = "java(getVariantPrice(cartItem))")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(cartItem))")
    @Mapping(target = "stockQuantity", expression = "java(getVariantStock(cartItem))")
    @Mapping(target = "outOfStock", expression = "java(isOutOfStock(cartItem))")
    @Mapping(target = "overStock", expression = "java(isOverStock(cartItem))")
    CartItemResponse toResponse(CartItem cartItem);

    default BigDecimal getVariantPrice(CartItem cartItem) {
        ProductTemplate template = matchingTemplate(cartItem);
        if (template != null && template.getPrice() != null) {
            return template.getPrice();
        }
        return BigDecimal.ZERO;
    }

    default Integer getVariantStock(CartItem cartItem) {
        ProductTemplate template = matchingTemplate(cartItem);
        if (template == null || template.getStockQuantity() == null) {
            return 0;
        }
        return template.getStockQuantity();
    }

    default BigDecimal calculateSubtotal(CartItem cartItem) {
        return getVariantPrice(cartItem).multiply(BigDecimal.valueOf(cartItem.getQuantity()));
    }

    default boolean isOutOfStock(CartItem cartItem) {
        return getVariantStock(cartItem) == 0;
    }

    default boolean isOverStock(CartItem cartItem) {
        return cartItem.getQuantity() > getVariantStock(cartItem);
    }

    default ProductTemplate matchingTemplate(CartItem cartItem) {
        if (cartItem.getProduct() == null || cartItem.getProduct().getTemplates() == null) {
            return null;
        }
        return cartItem.getProduct().getTemplates().stream()
                .filter(template -> Boolean.TRUE.equals(template.getStatus()))
                .filter(template -> same(template.getColor(), cartItem.getColor())
                        && same(template.getSize(), cartItem.getSize()))
                .findFirst()
                .orElse(null);
    }

    private static boolean same(String left, String right) {
        String a = left == null || left.isBlank() ? null : left.trim();
        String b = right == null || right.isBlank() ? null : right.trim();
        if (a == null || b == null) {
            return a == null && b == null;
        }
        return a.equalsIgnoreCase(b);
    }
}
