package vn.edu.hcmute.fashion.catalog.service;

import vn.edu.hcmute.fashion.catalog.entity.Product;
import vn.edu.hcmute.fashion.catalog.entity.ProductStatus;
import vn.edu.hcmute.fashion.catalog.entity.ProductVariant;
import jakarta.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecs {
    private ProductSpecs() {}

    /** Admin mặc định không thấy sản phẩm đã xóa mềm. */
    public static Specification<Product> notDeleted() {
        return (root, query, cb) -> cb.notEqual(root.get("status"), ProductStatus.DELETED);
    }

    public static Specification<Product> filter(String keyword, Long categoryId, Long brandId,
                                                BigDecimal minPrice, BigDecimal maxPrice, ProductStatus status) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (status != null) p.add(cb.equal(root.get("status"), status));
            if (keyword != null && !keyword.isBlank())
                p.add(cb.like(cb.lower(root.get("name")), "%" + keyword.trim().toLowerCase() + "%"));
            if (categoryId != null) p.add(cb.equal(root.get("category").get("id"), categoryId));
            if (brandId != null) p.add(cb.equal(root.get("brand").get("id"), brandId));
            if (minPrice != null || maxPrice != null) {
                Subquery<Long> sq = query.subquery(Long.class);
                Root<ProductVariant> v = sq.from(ProductVariant.class);
                List<Predicate> vp = new ArrayList<>();
                vp.add(cb.equal(v.get("product"), root));
                if (minPrice != null) vp.add(cb.greaterThanOrEqualTo(v.<BigDecimal>get("price"), minPrice));
                if (maxPrice != null) vp.add(cb.lessThanOrEqualTo(v.<BigDecimal>get("price"), maxPrice));
                sq.select(v.<Long>get("id")).where(vp.toArray(new Predicate[0]));
                p.add(cb.exists(sq));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}
