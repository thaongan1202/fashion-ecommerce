package vn.edu.hcmute.fashion.catalog.service;

import vn.edu.hcmute.fashion.catalog.dto.CatalogDtos.*;
import com.team.shop.catalog.entity.*;
import com.team.shop.catalog.repository.*;
import vn.edu.hcmute.fashion.catalog.entity.*;
import vn.edu.hcmute.fashion.catalog.repository.*;
import vn.edu.hcmute.fashion.common.ApiException;
import vn.edu.hcmute.fashion.common.PageResponse;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class ProductService {
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final BrandRepository brands;
    private final ProductVariantRepository variants;
    private final ProductImageRepository images;
    private final FileStorageService storage;

    public ProductService(ProductRepository products, CategoryRepository categories, BrandRepository brands,
                          ProductVariantRepository variants, ProductImageRepository images, FileStorageService storage) {
        this.products = products; this.categories = categories; this.brands = brands;
        this.variants = variants; this.images = images; this.storage = storage;
    }

    // ================= Public (Guest/Customer) =================
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> listPublic(String keyword, Long categoryId, Long brandId,
                                                    BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        var spec = ProductSpecs.filter(keyword, categoryId, brandId, minPrice, maxPrice, ProductStatus.ACTIVE);
        var result = products.findAll(spec, pageable(page, size));
        return PageResponse.of(result.map(p -> toResponse(p, false)));
    }

    @Transactional(readOnly = true)
    public ProductResponse getPublic(Long id) {
        Product p = find(id);
        if (p.getStatus() != ProductStatus.ACTIVE) throw ApiException.notFound("Sản phẩm không tồn tại");
        return toResponse(p, false);
    }

    // ================= Admin =================
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> listAdmin(String keyword, Long categoryId, Long brandId,
                                                   ProductStatus status, int page, int size) {
        var spec = ProductSpecs.filter(keyword, categoryId, brandId, null, null, status);
        return PageResponse.of(products.findAll(spec, pageable(page, size)).map(p -> toResponse(p, true)));
    }

    @Transactional(readOnly = true)
    public ProductResponse getAdmin(Long id) { return toResponse(find(id), true); }

    public ProductResponse create(ProductRequest r) {
        Product p = new Product();
        apply(p, r);
        return toResponse(products.save(p), true);
    }

    public ProductResponse update(Long id, ProductRequest r) {
        Product p = find(id);
        apply(p, r);
        return toResponse(p, true);
    }

    /** Soft-delete: chỉ chuyển sang HIDDEN để giữ lịch sử đơn hàng/review. */
    public void hide(Long id) { find(id).setStatus(ProductStatus.HIDDEN); }

    // ---- Variant ----
    public VariantResponse addVariant(Long productId, VariantRequest r) {
        Product p = find(productId);
        String sku = normalizeSku(r.sku());
        if (variants.existsBySku(sku)) throw ApiException.conflict("SKU đã tồn tại");
        ProductVariant v = new ProductVariant();
        v.setProduct(p);
        fill(v, r, sku);
        return toVariant(variants.save(v));
    }

    public VariantResponse updateVariant(Long id, VariantRequest r) {
        ProductVariant v = findVariant(id);
        String sku = normalizeSku(r.sku());
        if (variants.existsBySkuAndIdNot(sku, id)) throw ApiException.conflict("SKU đã tồn tại");
        fill(v, r, sku);
        return toVariant(v);
    }

    /** Ẩn variant (active=false) thay vì xóa cứng, vì đơn hàng có thể đã tham chiếu. */
    public void deactivateVariant(Long id) { findVariant(id).setActive(false); }

    // ---- Image ----
    public ImageResponse addImage(Long productId, MultipartFile file) {
        Product p = find(productId);
        String url = storage.store(file);
        ProductImage img = new ProductImage();
        img.setProduct(p);
        img.setUrl(url);
        img.setSortOrder(p.getImages().size());
        return toImage(images.save(img));
    }

    public void deleteImage(Long id) {
        ProductImage img = images.findById(id).orElseThrow(() -> ApiException.notFound("Ảnh không tồn tại"));
        storage.delete(img.getUrl());
        images.delete(img);
    }

    // ================= helpers =================
    private Product find(Long id) {
        return products.findById(id).orElseThrow(() -> ApiException.notFound("Sản phẩm không tồn tại"));
    }

    private ProductVariant findVariant(Long id) {
        return variants.findById(id).orElseThrow(() -> ApiException.notFound("Variant không tồn tại"));
    }

    private PageRequest pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "id"));
    }

    private void apply(Product p, ProductRequest r) {
        p.setName(r.name().trim());
        p.setDescription(r.description());
        p.setCategory(categories.findById(r.categoryId()).orElseThrow(() -> ApiException.notFound("Danh mục không tồn tại")));
        p.setBrand(brands.findById(r.brandId()).orElseThrow(() -> ApiException.notFound("Thương hiệu không tồn tại")));
        if (r.status() != null) p.setStatus(r.status());
    }

    private String normalizeSku(String sku) { return sku.trim().toUpperCase(); }

    private void fill(ProductVariant v, VariantRequest r, String sku) {
        v.setSku(sku);
        v.setSize(blankToNull(r.size()));
        v.setColor(blankToNull(r.color()));
        v.setPrice(r.price());
        v.setStock(r.stock());
        v.setActive(true);
    }

    private String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }

    private ProductResponse toResponse(Product p, boolean admin) {
        List<ProductVariant> vs = p.getVariants().stream().filter(v -> admin || v.isActive()).toList();
        BigDecimal min = vs.stream().filter(ProductVariant::isActive).map(ProductVariant::getPrice)
                .min(Comparator.naturalOrder()).orElse(null);
        Category c = p.getCategory();
        Brand b = p.getBrand();
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getStatus(),
                new NamedResponse(c.getId(), c.getName(), c.getDescription()),
                new NamedResponse(b.getId(), b.getName(), b.getDescription()),
                min, vs.stream().map(this::toVariant).toList(), p.getImages().stream().map(this::toImage).toList());
    }

    private VariantResponse toVariant(ProductVariant v) {
        return new VariantResponse(v.getId(), v.getSku(), v.getSize(), v.getColor(), v.getPrice(), v.getStock(), v.isActive());
    }

    private ImageResponse toImage(ProductImage i) { return new ImageResponse(i.getId(), i.getUrl(), i.getSortOrder()); }
}
