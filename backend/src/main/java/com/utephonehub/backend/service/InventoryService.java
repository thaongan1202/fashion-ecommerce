package com.utephonehub.backend.service;

import com.utephonehub.backend.entity.Product;
import com.utephonehub.backend.entity.ProductTemplate;
import com.utephonehub.backend.exception.BadRequestException;
import com.utephonehub.backend.repository.ProductTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Điều chỉnh tồn kho theo đúng biến thể màu/size khách đã chọn.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductTemplateRepository productTemplateRepository;

    public int availableStock(Product product, String color, String size) {
        ProductTemplate template = findVariant(product, color, size);
        return template.getStockQuantity() == null ? 0 : template.getStockQuantity();
    }

    public BigDecimal priceOf(Product product, String color, String size) {
        ProductTemplate template = findVariant(product, color, size);
        if (template.getPrice() == null || template.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Đơn giá sản phẩm không hợp lệ");
        }
        return template.getPrice();
    }

    public void deduct(Product product, String color, String size, int quantity) {
        if (quantity < 1) {
            throw new BadRequestException("Số lượng phải lớn hơn hoặc bằng 1 và không được âm");
        }
        ProductTemplate template = findVariant(product, color, size);
        int stock = template.getStockQuantity() == null ? 0 : template.getStockQuantity();
        if (quantity > stock) {
            throw new BadRequestException(String.format(
                    "Sản phẩm '%s' chỉ còn %d trong kho. Không thể đặt số lượng lớn hơn hàng còn.",
                    product.getName(), stock));
        }
        template.setStockQuantity(stock - quantity);
        productTemplateRepository.save(template);
        log.info("Deducted {} from SKU {} ({} -> {})", quantity, template.getSku(), stock, template.getStockQuantity());
    }

    public void restore(Product product, String color, String size, int quantity) {
        if (quantity < 1 || product == null || product.getTemplates() == null || product.getTemplates().isEmpty()) {
            return;
        }
        ProductTemplate template;
        try {
            template = findVariant(product, color, size);
        } catch (BadRequestException ex) {
            template = product.getTemplates().stream()
                    .filter(item -> Boolean.TRUE.equals(item.getStatus()))
                    .findFirst()
                    .orElse(product.getTemplates().get(0));
        }
        int stock = template.getStockQuantity() == null ? 0 : template.getStockQuantity();
        template.setStockQuantity(stock + quantity);
        productTemplateRepository.save(template);
        log.info("Restored {} to SKU {} ({} -> {})", quantity, template.getSku(), stock, template.getStockQuantity());
    }

    public ProductTemplate findVariant(Product product, String color, String size) {
        if (product.getTemplates() == null || product.getTemplates().isEmpty()) {
            throw new BadRequestException("Sản phẩm không có biến thể trong kho");
        }
        String normalizedColor = normalize(color);
        String normalizedSize = normalize(size);
        return product.getTemplates().stream()
                .filter(template -> Boolean.TRUE.equals(template.getStatus()))
                .filter(template -> same(template.getColor(), normalizedColor) && same(template.getSize(), normalizedSize))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Không tìm thấy phiên bản sản phẩm với màu và kích thước đã chọn"));
    }

    private static String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private static boolean same(String left, String right) {
        String a = normalize(left);
        String b = normalize(right);
        if (a == null || b == null) {
            return a == null && b == null;
        }
        return a.equalsIgnoreCase(b);
    }
}
