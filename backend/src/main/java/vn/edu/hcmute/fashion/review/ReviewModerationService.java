package vn.edu.hcmute.fashion.review;

import static vn.edu.hcmute.fashion.review.AdminReviewDtos.*;

import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.fashion.shared.AdminException;

@Service
public class ReviewModerationService {
    private final JdbcTemplate jdbc;
    public ReviewModerationService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional(readOnly = true)
    public ReviewPage list(String adminEmail, String requestedStatus, int page, int size) {
        requireAdmin(adminEmail);
        String status = normalizeStatus(requestedStatus, true);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String condition = status == null ? "" : " WHERE r.status = ?";
        Long count = status == null
                ? jdbc.queryForObject("SELECT count(*) FROM reviews", Long.class)
                : jdbc.queryForObject("SELECT count(*) FROM reviews r WHERE r.status = ?", Long.class, status);
        String sql = """
                SELECT r.id, r.product_id, p.name AS product_name, r.user_id, u.full_name AS customer_name,
                       u.email AS customer_email, r.rating, r.comment, r.image_url, r.status, r.created_at
                FROM reviews r JOIN products p ON p.id = r.product_id JOIN users u ON u.id = r.user_id
                """ + condition + " ORDER BY r.created_at DESC, r.id DESC LIMIT ? OFFSET ?";
        List<ReviewItem> rows = status == null
                ? jdbc.query(sql, this::map, safeSize, (long) safePage * safeSize)
                : jdbc.query(sql, this::map, status, safeSize, (long) safePage * safeSize);
        long total = count == null ? 0 : count;
        return new ReviewPage(rows, total, safePage, safeSize, (int) Math.ceil((double) total / safeSize));
    }

    @Transactional
    public ReviewItem changeStatus(String adminEmail, long reviewId, String requestedStatus) {
        requireAdmin(adminEmail);
        String status = normalizeStatus(requestedStatus, false);
        int changed = jdbc.update("UPDATE reviews SET status = ? WHERE id = ?", status, reviewId);
        if (changed == 0) throw new AdminException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy đánh giá.");
        return jdbc.query("""
                SELECT r.id, r.product_id, p.name AS product_name, r.user_id, u.full_name AS customer_name,
                       u.email AS customer_email, r.rating, r.comment, r.image_url, r.status, r.created_at
                FROM reviews r JOIN products p ON p.id = r.product_id JOIN users u ON u.id = r.user_id
                WHERE r.id = ?
                """, this::map, reviewId).get(0);
    }

    private long requireAdmin(String email) {
        if (email == null || email.isBlank() || "anonymousUser".equals(email))
            throw new AdminException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Vui lòng đăng nhập.");
        var ids = jdbc.query("SELECT id FROM users WHERE lower(email) = lower(?) AND status = 'ACTIVE' AND role = 'ADMIN'",
                (rs, row) -> rs.getLong(1), email.trim());
        if (ids.isEmpty()) throw new AdminException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Chỉ Admin mới được kiểm duyệt đánh giá.");
        return ids.get(0);
    }

    private String normalizeStatus(String value, boolean allowAll) {
        if (value == null || value.isBlank()) return allowAll ? "PENDING" : null;
        String status = value.trim().toUpperCase(Locale.ROOT);
        if (allowAll && "ALL".equals(status)) return null;
        if (allowAll && !"PENDING".equals(status) && !"APPROVED".equals(status) && !"HIDDEN".equals(status))
            throw badStatus("status phải là PENDING, APPROVED, HIDDEN hoặc ALL.");
        if (!allowAll && !"APPROVED".equals(status) && !"HIDDEN".equals(status))
            throw badStatus("Trạng thái duyệt chỉ được là APPROVED hoặc HIDDEN.");
        return status;
    }

    private ReviewItem map(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
        return new ReviewItem(rs.getLong("id"), rs.getLong("product_id"), rs.getString("product_name"),
                rs.getLong("user_id"), rs.getString("customer_name"), rs.getString("customer_email"),
                rs.getInt("rating"), rs.getString("comment"), rs.getString("image_url"), rs.getString("status"),
                rs.getObject("created_at", java.time.OffsetDateTime.class));
    }

    private static AdminException badStatus(String message) { return new AdminException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message); }
}
