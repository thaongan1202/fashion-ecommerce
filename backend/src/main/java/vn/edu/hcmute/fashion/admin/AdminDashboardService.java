package vn.edu.hcmute.fashion.admin;

import static vn.edu.hcmute.fashion.admin.AdminDashboardDtos.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.fashion.shared.AdminException;

@Service
public class AdminDashboardService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final JdbcTemplate jdbc;
    public AdminDashboardService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional(readOnly = true)
    public DashboardSummary get(String adminEmail) {
        requireAdmin(adminEmail);
        var metrics = jdbc.queryForMap("""
                SELECT
                  (SELECT count(*) FROM users WHERE role = 'CUSTOMER' AND status = 'ACTIVE') AS active_customers,
                  (SELECT count(*) FROM products WHERE status = 'ACTIVE') AS active_products,
                  (SELECT count(*) FROM orders) AS total_orders,
                  (SELECT count(*) FROM orders WHERE status = 'PENDING') AS pending_orders,
                  (SELECT count(*) FROM orders WHERE status = 'DELIVERED') AS delivered_orders,
                  (SELECT count(*) FROM product_variants pv JOIN products p ON p.id = pv.product_id
                    WHERE pv.stock_qty <= 5 AND p.status = 'ACTIVE') AS low_stock_variants,
                  (SELECT COALESCE(sum(total_amount), 0) FROM orders WHERE status = 'DELIVERED') AS delivered_revenue
                """);
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate start = today.minusDays(6);
        Map<LocalDate, DailyRevenue> byDate = new HashMap<>();
        jdbc.query("""
                SELECT (h.changed_at AT TIME ZONE 'Asia/Ho_Chi_Minh')::date AS business_date,
                       COALESCE(sum(o.total_amount), 0) AS revenue, count(DISTINCT o.id) AS order_count
                FROM order_status_history h JOIN orders o ON o.id = h.order_id
                WHERE h.status = 'DELIVERED'
                  AND (h.changed_at AT TIME ZONE 'Asia/Ho_Chi_Minh')::date BETWEEN ? AND ?
                GROUP BY business_date ORDER BY business_date
                """, rs -> {
            LocalDate date = rs.getObject("business_date", Date.class).toLocalDate();
            byDate.put(date, new DailyRevenue(date, rs.getBigDecimal("revenue"), rs.getLong("order_count")));
        }, Date.valueOf(start), Date.valueOf(today));
        List<DailyRevenue> daily = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            daily.add(byDate.getOrDefault(date, new DailyRevenue(date, BigDecimal.ZERO.setScale(2), 0)));
        }
        return new DashboardSummary(number(metrics, "active_customers"), number(metrics, "active_products"),
                number(metrics, "total_orders"), number(metrics, "pending_orders"), number(metrics, "delivered_orders"),
                number(metrics, "low_stock_variants"), (BigDecimal) metrics.get("delivered_revenue"), daily);
    }

    private long requireAdmin(String email) {
        if (email == null || email.isBlank() || "anonymousUser".equals(email))
            throw new AdminException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Vui lòng đăng nhập.");
        Boolean allowed = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM users WHERE lower(email) = lower(?) AND status = 'ACTIVE' AND role = 'ADMIN')", Boolean.class, email.trim());
        if (!Boolean.TRUE.equals(allowed)) throw new AdminException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Chỉ Admin mới được xem dashboard.");
        return 1;
    }

    private long number(Map<String, Object> row, String key) { return ((Number) row.get(key)).longValue(); }
}
