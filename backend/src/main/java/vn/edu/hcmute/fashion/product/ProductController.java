package vn.edu.hcmute.fashion.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) { this.service = service; }

    @GetMapping
    public ProductService.ProductPage list(@RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) Long categoryId, @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return service.list(query, categoryId, brandId, minPrice, maxPrice, page, size, sort);
    }

    @GetMapping("/categories")
    public java.util.List<ProductService.CategoryOption> categories() { return service.categories(); }

    @GetMapping("/price-range")
    public ProductService.PriceRange priceRange() { return service.priceRange(); }

    @GetMapping("/{productId}")
    public ProductResponse get(@PathVariable long productId) { return service.get(productId); }

    @GetMapping("/{productId}/related")
    public java.util.List<ProductResponse> related(@PathVariable long productId) { return service.related(productId); }
}
