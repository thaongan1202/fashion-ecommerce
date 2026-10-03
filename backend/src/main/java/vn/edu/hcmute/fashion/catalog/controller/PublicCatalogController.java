package vn.edu.hcmute.fashion.catalog.controller;

import vn.edu.hcmute.fashion.catalog.dto.CatalogDtos.*;
import vn.edu.hcmute.fashion.catalog.service.CatalogService;
import vn.edu.hcmute.fashion.catalog.service.ProductService;
import vn.edu.hcmute.fashion.common.PageResponse;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/** API public cho Guest/Customer (Member 3 dùng cho trang sản phẩm + review). */
@RestController
@RequestMapping("/api/catalog-public")
public class PublicCatalogController {
    private final CatalogService catalog;
    private final ProductService products;

    public PublicCatalogController(CatalogService catalog, ProductService products) {
        this.catalog = catalog; this.products = products;
    }

    @GetMapping("/categories")
    public List<NamedResponse> categories() { return catalog.listCategories(); }

    @GetMapping("/brands")
    public List<NamedResponse> brands() { return catalog.listBrands(); }

    @GetMapping("/products")
    public PageResponse<ProductResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return products.listPublic(keyword, categoryId, brandId, minPrice, maxPrice, page, size);
    }

    @GetMapping("/products/{id}")
    public ProductResponse detail(@PathVariable Long id) { return products.getPublic(id); }
}
