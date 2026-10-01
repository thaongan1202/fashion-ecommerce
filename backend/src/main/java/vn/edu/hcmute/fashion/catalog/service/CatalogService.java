package vn.edu.hcmute.fashion.catalog.service;

import vn.edu.hcmute.fashion.catalog.dto.CatalogDtos.*;
import vn.edu.hcmute.fashion.catalog.entity.Brand;
import vn.edu.hcmute.fashion.catalog.entity.Category;
import vn.edu.hcmute.fashion.catalog.repository.*;
import vn.edu.hcmute.fashion.common.ApiException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Category + Brand CRUD. */
@Service
@Transactional
public class CatalogService {
    private final CategoryRepository categories;
    private final BrandRepository brands;
    private final ProductRepository products;

    public CatalogService(CategoryRepository c, BrandRepository b, ProductRepository p) {
        this.categories = c; this.brands = b; this.products = p;
    }

    // ---- Category ----
    @Transactional(readOnly = true)
    public List<NamedResponse> listCategories() {
        return categories.findAll().stream().map(c -> new NamedResponse(c.getId(), c.getName(), c.getDescription())).toList();
    }

    public NamedResponse createCategory(NamedRequest r) {
        String name = r.name().trim();
        if (categories.existsByNameIgnoreCase(name)) throw ApiException.conflict("Tên danh mục đã tồn tại");
        Category c = new Category();
        c.setName(name); c.setDescription(r.description());
        c = categories.save(c);
        return new NamedResponse(c.getId(), c.getName(), c.getDescription());
    }

    public NamedResponse updateCategory(Long id, NamedRequest r) {
        Category c = categories.findById(id).orElseThrow(() -> ApiException.notFound("Danh mục không tồn tại"));
        String name = r.name().trim();
        if (categories.existsByNameIgnoreCaseAndIdNot(name, id)) throw ApiException.conflict("Tên danh mục đã tồn tại");
        c.setName(name); c.setDescription(r.description());
        return new NamedResponse(c.getId(), c.getName(), c.getDescription());
    }

    public void deleteCategory(Long id) {
        Category c = categories.findById(id).orElseThrow(() -> ApiException.notFound("Danh mục không tồn tại"));
        if (products.existsByCategoryId(id)) throw ApiException.conflict("Không thể xóa danh mục khi còn sản phẩm");
        categories.delete(c);
    }

    // ---- Brand ----
    @Transactional(readOnly = true)
    public List<NamedResponse> listBrands() {
        return brands.findAll().stream().map(b -> new NamedResponse(b.getId(), b.getName(), b.getDescription())).toList();
    }

    public NamedResponse createBrand(NamedRequest r) {
        String name = r.name().trim();
        if (brands.existsByNameIgnoreCase(name)) throw ApiException.conflict("Tên thương hiệu đã tồn tại");
        Brand b = new Brand();
        b.setName(name); b.setDescription(r.description());
        b = brands.save(b);
        return new NamedResponse(b.getId(), b.getName(), b.getDescription());
    }

    public NamedResponse updateBrand(Long id, NamedRequest r) {
        Brand b = brands.findById(id).orElseThrow(() -> ApiException.notFound("Thương hiệu không tồn tại"));
        String name = r.name().trim();
        if (brands.existsByNameIgnoreCaseAndIdNot(name, id)) throw ApiException.conflict("Tên thương hiệu đã tồn tại");
        b.setName(name); b.setDescription(r.description());
        return new NamedResponse(b.getId(), b.getName(), b.getDescription());
    }

    public void deleteBrand(Long id) {
        Brand b = brands.findById(id).orElseThrow(() -> ApiException.notFound("Thương hiệu không tồn tại"));
        if (products.existsByBrandId(id)) throw ApiException.conflict("Không thể xóa thương hiệu khi còn sản phẩm");
        brands.delete(b);
    }
}
