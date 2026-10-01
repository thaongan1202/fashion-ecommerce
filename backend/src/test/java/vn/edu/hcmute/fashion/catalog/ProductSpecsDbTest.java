package vn.edu.hcmute.fashion.catalog;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import vn.edu.hcmute.fashion.catalog.entity.*;
import vn.edu.hcmute.fashion.catalog.repository.*;
import vn.edu.hcmute.fashion.catalog.service.ProductSpecs;

/**
 * Test filter/search với PostgreSQL thật (schema từ Flyway V1) ở database test riêng.
 * Mỗi test rollback; dữ liệu test dùng tiền tố "zztest" để không lẫn với seed của nhóm.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ProductSpecsDbTest {
    @Autowired TestEntityManager em;
    @Autowired ProductRepository products;

    Category shirts, shoes;
    Brand nike, adidas;

    @BeforeEach
    void data() {
        shirts = category("zztest-ao");
        shoes = category("zztest-giay");
        nike = brand("zztest-nike");
        adidas = brand("zztest-adidas");

        product("zztest Áo thun", shirts, nike, ProductStatus.ACTIVE, "400000");
        product("zztest Giày chạy", shoes, adidas, ProductStatus.ACTIVE, "3000000");
        product("zztest Áo ẩn", shirts, nike, ProductStatus.INACTIVE, "300000");
        product("zztest Áo đã xóa", shirts, nike, ProductStatus.DELETED, "300000");
        em.flush();
        em.clear();
    }

    private List<String> names(String keyword, Long cat, Long brand, String min, String max, ProductStatus status) {
        var spec = ProductSpecs.filter(keyword, cat, brand,
                min == null ? null : new BigDecimal(min), max == null ? null : new BigDecimal(max), status);
        return products.findAll(spec).stream().map(Product::getName).sorted().toList();
    }

    @Test
    void publicQuery_hidesInactiveAndDeleted() {
        assertEquals(List.of("zztest Giày chạy", "zztest Áo thun"),
                names("zztest", null, null, null, null, ProductStatus.ACTIVE));
    }

    @Test
    void keywordSearch_isCaseInsensitive() {
        assertEquals(List.of("zztest Giày chạy"), names("ZZTEST GIÀY", null, null, null, null, ProductStatus.ACTIVE));
    }

    @Test
    void filterByCategoryAndBrand() {
        assertEquals(List.of("zztest Áo thun"), names("zztest", shirts.getId(), null, null, null, ProductStatus.ACTIVE));
        assertEquals(List.of("zztest Giày chạy"), names("zztest", null, adidas.getId(), null, null, ProductStatus.ACTIVE));
    }

    @Test
    void filterByPriceRange() {
        assertEquals(List.of("zztest Giày chạy"), names("zztest", null, null, "1000000", null, ProductStatus.ACTIVE));
        assertEquals(List.of("zztest Áo thun"), names("zztest", null, null, null, "500000", ProductStatus.ACTIVE));
    }

    @Test
    void adminDefault_excludesDeletedButKeepsInactive() {
        var spec = ProductSpecs.filter("zztest", null, null, null, null, null).and(ProductSpecs.notDeleted());
        List<String> found = products.findAll(spec).stream().map(Product::getName).sorted().toList();
        assertEquals(List.of("zztest Giày chạy", "zztest Áo thun", "zztest Áo ẩn"), found);
    }

    // ---- helpers ----
    private Category category(String name) { Category c = new Category(); c.setName(name); return em.persist(c); }
    private Brand brand(String name) { Brand b = new Brand(); b.setName(name); return em.persist(b); }

    private void product(String name, Category c, Brand b, ProductStatus status, String price) {
        Product p = new Product();
        p.setName(name); p.setCategory(c); p.setBrand(b); p.setStatus(status);
        em.persist(p);
        ProductVariant v = new ProductVariant();
        v.setProduct(p); v.setSku(name.toUpperCase().replace(' ', '-')); v.setPrice(new BigDecimal(price)); v.setStockQty(1);
        em.persist(v);
    }
}
