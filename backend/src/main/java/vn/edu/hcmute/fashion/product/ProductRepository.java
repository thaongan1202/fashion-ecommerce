package vn.edu.hcmute.fashion.product;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class ProductRepository {
    private final JdbcTemplate jdbc;

    ProductRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    long count(String query, Long categoryId, Long brandId, BigDecimal minPrice, BigDecimal maxPrice) {
        var sql = new StringBuilder("SELECT count(*) FROM products p WHERE p.status = 'ACTIVE'");
        var params = new ArrayList<Object>();
        appendFilters(sql, params, query, categoryId, brandId, minPrice, maxPrice);
        return jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
    }

    List<ProductResponse> findPage(String query, Long categoryId, Long brandId, BigDecimal minPrice, BigDecimal maxPrice,
            String sortColumn, String sortDirection, int size, int offset) {
        var sql = new StringBuilder("""
                SELECT p.id, p.name, p.description, p.category_id, c.name AS category_name,
                       p.brand_id, b.name AS brand_name, p.material,
                       COALESCE((SELECT min(v.price) FROM product_variants v WHERE v.product_id = p.id), 0) AS min_price,
                       COALESCE((SELECT max(v.price) FROM product_variants v WHERE v.product_id = p.id), 0) AS max_price,
                       COALESCE((SELECT avg(r.rating)::double precision FROM reviews r WHERE r.product_id = p.id AND r.status = 'APPROVED'), 0) AS average_rating,
                       (SELECT count(*) FROM reviews r WHERE r.product_id = p.id AND r.status = 'APPROVED') AS review_count
                FROM products p JOIN categories c ON c.id = p.category_id JOIN brands b ON b.id = p.brand_id
                WHERE p.status = 'ACTIVE'
                """);
        var params = new ArrayList<Object>();
        appendFilters(sql, params, query, categoryId, brandId, minPrice, maxPrice);
        sql.append(" ORDER BY ").append(sortColumn).append(' ').append(sortDirection).append(" LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);
        return jdbc.query(sql.toString(), (rs, row) -> {
            long id = rs.getLong("id");
            return new ProductResponse(id, rs.getString("name"), rs.getString("description"),
                    rs.getLong("category_id"), rs.getString("category_name"), rs.getLong("brand_id"),
                    rs.getString("brand_name"), rs.getString("material"), rs.getBigDecimal("min_price"),
                    rs.getBigDecimal("max_price"), rs.getDouble("average_rating"), rs.getLong("review_count"),
                    images(id), variants(id));
        }, params.toArray());
    }

    ProductResponse findById(long id) {
        var rows = jdbc.query("""
                SELECT p.id, p.name, p.description, p.category_id, c.name AS category_name,
                       p.brand_id, b.name AS brand_name, p.material,
                       COALESCE((SELECT min(v.price) FROM product_variants v WHERE v.product_id = p.id), 0) AS min_price,
                       COALESCE((SELECT max(v.price) FROM product_variants v WHERE v.product_id = p.id), 0) AS max_price,
                       COALESCE((SELECT avg(r.rating)::double precision FROM reviews r WHERE r.product_id = p.id AND r.status = 'APPROVED'), 0) AS average_rating,
                       (SELECT count(*) FROM reviews r WHERE r.product_id = p.id AND r.status = 'APPROVED') AS review_count
                FROM products p JOIN categories c ON c.id = p.category_id JOIN brands b ON b.id = p.brand_id
                WHERE p.id = ? AND p.status = 'ACTIVE'
                """, (rs, row) -> new ProductResponse(rs.getLong("id"), rs.getString("name"), rs.getString("description"),
                rs.getLong("category_id"), rs.getString("category_name"), rs.getLong("brand_id"), rs.getString("brand_name"),
                rs.getString("material"), rs.getBigDecimal("min_price"), rs.getBigDecimal("max_price"),
                rs.getDouble("average_rating"), rs.getLong("review_count"), images(rs.getLong("id")), variants(rs.getLong("id"))), id);
        return rows.stream().findFirst().orElse(null);
    }

    List<ProductResponse> findRelated(long productId, int limit) {
        var category = jdbc.query("SELECT category_id FROM products WHERE id = ? AND status = 'ACTIVE'", (rs, row) -> rs.getLong(1), productId);
        if (category.isEmpty()) return List.of();
        return findPage(null, category.get(0), null, null, null, "p.created_at", "DESC", limit, 0).stream()
                .filter(product -> product.id() != productId).limit(limit).toList();
    }

    List<ProductService.CategoryOption> findCategories() {
        return jdbc.query("""
                SELECT DISTINCT c.id, c.name
                FROM categories c JOIN products p ON p.category_id = c.id
                WHERE p.status = 'ACTIVE'
                ORDER BY c.name
                """, (rs, row) -> new ProductService.CategoryOption(rs.getLong("id"), rs.getString("name")));
    }

    ProductService.PriceRange findPriceRange() {
        return jdbc.queryForObject("""
                SELECT COALESCE(min(v.price), 0) AS min_price, COALESCE(max(v.price), 0) AS max_price
                FROM product_variants v JOIN products p ON p.id = v.product_id
                WHERE p.status = 'ACTIVE'
                """, (rs, row) -> new ProductService.PriceRange(
                rs.getBigDecimal("min_price"), rs.getBigDecimal("max_price")));
    }

    private List<ProductResponse.ProductImageResponse> images(long productId) {
        return jdbc.query("SELECT id, image_url, is_primary FROM product_images WHERE product_id = ? ORDER BY is_primary DESC, id",
                (rs, row) -> new ProductResponse.ProductImageResponse(rs.getLong("id"), rs.getString("image_url"), rs.getBoolean("is_primary")), productId);
    }

    private List<ProductResponse.ProductVariantResponse> variants(long productId) {
        return jdbc.query("SELECT id, size, color, price, stock_qty FROM product_variants WHERE product_id = ? ORDER BY id",
                (rs, row) -> new ProductResponse.ProductVariantResponse(rs.getLong("id"), rs.getString("size"), rs.getString("color"), rs.getBigDecimal("price"), rs.getInt("stock_qty")), productId);
    }

    private void appendFilters(StringBuilder sql, List<Object> params, String query, Long categoryId, Long brandId,
            BigDecimal minPrice, BigDecimal maxPrice) {
        if (query != null && !query.isBlank()) {
            sql.append(" AND (p.name ILIKE ? OR p.description ILIKE ?)");
            params.add("%" + query.trim() + "%");
            params.add("%" + query.trim() + "%");
        }
        if (categoryId != null) { sql.append(" AND p.category_id = ?"); params.add(categoryId); }
        if (brandId != null) { sql.append(" AND p.brand_id = ?"); params.add(brandId); }
        if (minPrice != null) {
            sql.append(" AND COALESCE((SELECT min(v.price) FROM product_variants v WHERE v.product_id = p.id), 0) >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND COALESCE((SELECT min(v.price) FROM product_variants v WHERE v.product_id = p.id), 0) <= ?");
            params.add(maxPrice);
        }
    }
}
