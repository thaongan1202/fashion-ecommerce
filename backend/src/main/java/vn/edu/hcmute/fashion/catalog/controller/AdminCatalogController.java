package vn.edu.hcmute.fashion.catalog.controller;

import vn.edu.hcmute.fashion.catalog.dto.CatalogDtos.*;
import vn.edu.hcmute.fashion.catalog.entity.ProductStatus;
import vn.edu.hcmute.fashion.catalog.service.CatalogService;
import vn.edu.hcmute.fashion.catalog.service.ProductService;
import vn.edu.hcmute.fashion.common.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Mọi đường dẫn /admin/** do SecurityConfig (Member 1) yêu cầu role ADMIN. */
@RestController
@RequestMapping("/admin")
public class AdminCatalogController {
    private final CatalogService catalog;
    private final ProductService products;

    public AdminCatalogController(CatalogService catalog, ProductService products) {
        this.catalog = catalog; this.products = products;
    }

    // ----- Category -----
    @GetMapping("/categories")
    public List<NamedResponse> categories() { return catalog.listCategories(); }

    @PostMapping("/categories") @ResponseStatus(HttpStatus.CREATED)
    public NamedResponse createCategory(@Valid @RequestBody NamedRequest r) { return catalog.createCategory(r); }

    @PutMapping("/categories/{id}")
    public NamedResponse updateCategory(@PathVariable Long id, @Valid @RequestBody NamedRequest r) {
        return catalog.updateCategory(id, r);
    }

    @DeleteMapping("/categories/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) { catalog.deleteCategory(id); }

    // ----- Brand -----
    @GetMapping("/brands")
    public List<NamedResponse> brands() { return catalog.listBrands(); }

    @PostMapping("/brands") @ResponseStatus(HttpStatus.CREATED)
    public NamedResponse createBrand(@Valid @RequestBody NamedRequest r) { return catalog.createBrand(r); }

    @PutMapping("/brands/{id}")
    public NamedResponse updateBrand(@PathVariable Long id, @Valid @RequestBody NamedRequest r) {
        return catalog.updateBrand(id, r);
    }

    @DeleteMapping("/brands/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBrand(@PathVariable Long id) { catalog.deleteBrand(id); }

    // ----- Product -----
    @GetMapping("/products")
    public PageResponse<ProductResponse> listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return products.listAdmin(keyword, categoryId, brandId, status, page, size);
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) { return products.getAdmin(id); }

    @PostMapping("/products") @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest r) { return products.create(r); }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest r) {
        return products.update(id, r);
    }

    /** Soft-delete: đặt status = HIDDEN. Muốn hiện lại thì PUT với status = ACTIVE. */
    @DeleteMapping("/products/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hideProduct(@PathVariable Long id) { products.hide(id); }

    // ----- Variant -----
    @PostMapping("/products/{productId}/variants") @ResponseStatus(HttpStatus.CREATED)
    public VariantResponse addVariant(@PathVariable Long productId, @Valid @RequestBody VariantRequest r) {
        return products.addVariant(productId, r);
    }

    @PutMapping("/variants/{id}")
    public VariantResponse updateVariant(@PathVariable Long id, @Valid @RequestBody VariantRequest r) {
        return products.updateVariant(id, r);
    }

    @DeleteMapping("/variants/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateVariant(@PathVariable Long id) { products.deactivateVariant(id); }

    // ----- Image upload -----
    @PostMapping(value = "/products/{productId}/images", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public ImageResponse uploadImage(@PathVariable Long productId, @RequestParam("file") MultipartFile file) {
        return products.addImage(productId, file);
    }

    @DeleteMapping("/images/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@PathVariable Long id) { products.deleteImage(id); }
}
