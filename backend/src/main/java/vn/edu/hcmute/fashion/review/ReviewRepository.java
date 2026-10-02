package vn.edu.hcmute.fashion.review;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class ReviewRepository {
    private static final String REVIEW_SELECT = """
            SELECT r.id, u.full_name, r.rating, r.comment, r.image_url, r.status,
                   r.variant_id, pv.size AS purchased_size, pv.color AS purchased_color,
                   r.height_cm, r.weight_kg, r.created_at
            FROM reviews r
            JOIN users u ON u.id = r.user_id
            LEFT JOIN product_variants pv ON pv.id = r.variant_id
            """;

    private final JdbcTemplate jdbc;
    ReviewRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    boolean productExists(long productId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM products WHERE id = ? AND status = 'ACTIVE')", Boolean.class, productId));
    }

    List<PurchasedVariant> findDeliveredVariants(long userId, long productId) {
        return jdbc.query("""
                SELECT DISTINCT pv.id, pv.size, pv.color
                FROM orders o
                JOIN order_items oi ON oi.order_id = o.id
                JOIN product_variants pv ON pv.id = oi.variant_id
                WHERE o.user_id = ? AND o.status = 'DELIVERED' AND pv.product_id = ?
                ORDER BY pv.id
                """, (rs, row) -> new PurchasedVariant(rs.getLong("id"), rs.getString("size"), rs.getString("color")), userId, productId);
    }

    List<ReviewReminder> findDeliveredProductsWithoutReview(long userId) {
        return jdbc.query("""
                SELECT p.id AS product_id, p.name AS product_name,
                       max(coalesce((SELECT max(h.changed_at) FROM order_status_history h
                                     WHERE h.order_id = o.id AND h.status = 'DELIVERED'), o.created_at)) AS delivered_at
                FROM orders o
                JOIN order_items oi ON oi.order_id = o.id
                JOIN product_variants pv ON pv.id = oi.variant_id
                JOIN products p ON p.id = pv.product_id
                LEFT JOIN reviews r ON r.product_id = p.id AND r.user_id = o.user_id
                WHERE o.user_id = ? AND o.status = 'DELIVERED' AND r.id IS NULL
                GROUP BY p.id, p.name
                ORDER BY max(coalesce((SELECT max(h.changed_at) FROM order_status_history h
                                       WHERE h.order_id = o.id AND h.status = 'DELIVERED'), o.created_at)) DESC, p.id
                """, (rs, row) -> new ReviewReminder(rs.getLong("product_id"), rs.getString("product_name"),
                rs.getObject("delivered_at", java.time.OffsetDateTime.class)), userId);
    }

    boolean hasDeliveredVariant(long userId, long productId, long variantId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("""
                SELECT EXISTS (
                  SELECT 1 FROM orders o
                  JOIN order_items oi ON oi.order_id = o.id
                  JOIN product_variants pv ON pv.id = oi.variant_id
                  WHERE o.user_id = ? AND o.status = 'DELIVERED'
                    AND pv.product_id = ? AND pv.id = ?
                )
                """, Boolean.class, userId, productId, variantId));
    }

    boolean alreadyReviewed(long userId, long productId) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM reviews WHERE user_id = ? AND product_id = ?)", Boolean.class, userId, productId));
    }

    ReviewResponse findMine(long userId, long productId) {
        return jdbc.query(REVIEW_SELECT + " WHERE r.product_id = ? AND r.user_id = ?", ReviewRepository::mapReview, productId, userId)
                .stream().findFirst().orElse(null);
    }

    ReviewResponse create(long userId, long productId, int rating, String comment, String status,
            long variantId, Integer heightCm, BigDecimal weightKg, String imageUrl) {
        Long id = jdbc.queryForObject("""
                INSERT INTO reviews (product_id, user_id, rating, comment, status, variant_id, height_cm, weight_kg, image_url)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id
                """, Long.class, productId, userId, rating, comment.trim(), status, variantId, heightCm, weightKg, imageUrl);
        return jdbc.queryForObject(REVIEW_SELECT + " WHERE r.id = ?", ReviewRepository::mapReview, id);
    }

    ReviewResponse update(long reviewId, long userId, long productId, int rating, String comment, String status,
            long variantId, Integer heightCm, BigDecimal weightKg, String imageUrl) {
        int updated = jdbc.update("""
                UPDATE reviews SET rating = ?, comment = ?, status = ?, variant_id = ?, height_cm = ?, weight_kg = ?, image_url = ?
                WHERE id = ? AND user_id = ? AND product_id = ?
                """, rating, comment.trim(), status, variantId, heightCm, weightKg, imageUrl, reviewId, userId, productId);
        return updated == 0 ? null : findMine(userId, productId);
    }

    List<ReviewResponse> findApproved(long productId, int size, int offset) {
        return jdbc.query(REVIEW_SELECT + " WHERE r.product_id = ? AND r.status = 'APPROVED' "
                + "ORDER BY r.created_at DESC, r.id DESC LIMIT ? OFFSET ?", ReviewRepository::mapReview, productId, size, offset);
    }

    long countApproved(long productId) {
        return jdbc.queryForObject("SELECT count(*) FROM reviews WHERE product_id = ? AND status = 'APPROVED'", Long.class, productId);
    }

    boolean isActiveAdmin(long userId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM users WHERE id = ? AND role = 'ADMIN' AND status = 'ACTIVE')",
                Boolean.class, userId));
    }

    List<AdminReviewResponse> findPending() {
        return jdbc.query("""
                SELECT r.id, r.product_id, p.name AS product_name, u.full_name, r.rating, r.comment,
                       r.image_url, r.status, r.variant_id, pv.size, pv.color, r.height_cm, r.weight_kg, r.created_at
                FROM reviews r
                JOIN products p ON p.id = r.product_id
                JOIN users u ON u.id = r.user_id
                LEFT JOIN product_variants pv ON pv.id = r.variant_id
                WHERE r.status = 'PENDING'
                ORDER BY r.created_at ASC, r.id ASC
                """, (rs, row) -> new AdminReviewResponse(rs.getLong("id"), rs.getLong("product_id"),
                rs.getString("product_name"), rs.getString("full_name"), rs.getInt("rating"),
                rs.getString("comment"), rs.getString("image_url"), rs.getString("status"),
                (Long) rs.getObject("variant_id"), rs.getString("size"), rs.getString("color"),
                (Integer) rs.getObject("height_cm"), rs.getBigDecimal("weight_kg"),
                rs.getObject("created_at", java.time.OffsetDateTime.class)));
    }

    AdminReviewResponse changeStatus(long reviewId, String status) {
        int updated = jdbc.update("UPDATE reviews SET status = ? WHERE id = ? AND status = 'PENDING'", status, reviewId);
        if (updated == 0) return null;
        return jdbc.query("""
                SELECT r.id, r.product_id, p.name AS product_name, u.full_name, r.rating, r.comment,
                       r.image_url, r.status, r.variant_id, pv.size, pv.color, r.height_cm, r.weight_kg, r.created_at
                FROM reviews r
                JOIN products p ON p.id = r.product_id
                JOIN users u ON u.id = r.user_id
                LEFT JOIN product_variants pv ON pv.id = r.variant_id
                WHERE r.id = ?
                """, (rs, row) -> new AdminReviewResponse(rs.getLong("id"), rs.getLong("product_id"),
                rs.getString("product_name"), rs.getString("full_name"), rs.getInt("rating"),
                rs.getString("comment"), rs.getString("image_url"), rs.getString("status"),
                (Long) rs.getObject("variant_id"), rs.getString("size"), rs.getString("color"),
                (Integer) rs.getObject("height_cm"), rs.getBigDecimal("weight_kg"),
                rs.getObject("created_at", java.time.OffsetDateTime.class)), reviewId).stream().findFirst().orElse(null);
    }

    private static ReviewResponse mapReview(ResultSet rs, int row) throws SQLException {
        long variantId = rs.getLong("variant_id");
        Long nullableVariantId = rs.wasNull() ? null : variantId;
        int height = rs.getInt("height_cm");
        Integer nullableHeight = rs.wasNull() ? null : height;
        return new ReviewResponse(rs.getLong("id"), rs.getString("full_name"), rs.getInt("rating"),
                rs.getString("comment"), rs.getString("image_url"), rs.getString("status"), nullableVariantId,
                rs.getString("purchased_size"), rs.getString("purchased_color"), nullableHeight,
                rs.getBigDecimal("weight_kg"), rs.getObject("created_at", java.time.OffsetDateTime.class));
    }
}
