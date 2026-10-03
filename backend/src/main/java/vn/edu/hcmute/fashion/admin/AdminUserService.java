package vn.edu.hcmute.fashion.admin;

import static vn.edu.hcmute.fashion.admin.AdminUserDtos.*;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.hcmute.fashion.shared.AdminException;

@Service
public class AdminUserService {
    private final JdbcTemplate jdbc;
    public AdminUserService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional(readOnly = true)
    public UserPage list(String adminEmail, String keyword, String role, String status, int page, int size) {
        requireAdmin(adminEmail);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String normalizedRole = normalizeFilter(role, "CUSTOMER", "ADMIN");
        String normalizedStatus = normalizeFilter(status, "ACTIVE", "LOCKED");
        String search = keyword == null || keyword.isBlank() ? null : "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
        StringBuilder where = new StringBuilder(" WHERE TRUE");
        List<Object> args = new ArrayList<>();
        if (search != null) {
            where.append(" AND (lower(full_name) LIKE ? OR lower(email) LIKE ?)");
            args.add(search); args.add(search);
        }
        if (normalizedRole != null) { where.append(" AND role = ?"); args.add(normalizedRole); }
        if (normalizedStatus != null) { where.append(" AND status = ?"); args.add(normalizedStatus); }
        Long total = jdbc.queryForObject("SELECT count(*) FROM users" + where, Long.class, args.toArray());
        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(safeSize); pageArgs.add((long) safePage * safeSize);
        List<UserSummary> content = jdbc.query("""
                SELECT id, full_name, email, phone, role, status, created_at
                FROM users
                """ + where + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?",
                (rs, row) -> new UserSummary(rs.getLong("id"), rs.getString("full_name"), rs.getString("email"),
                        rs.getString("phone"), rs.getString("role"), rs.getString("status"),
                        rs.getObject("created_at", java.time.OffsetDateTime.class)),
                pageArgs.toArray());
        long count = total == null ? 0 : total;
        return new UserPage(content, count, safePage, safeSize, (int) Math.ceil((double) count / safeSize));
    }

    @Transactional
    public UserSummary changeStatus(String adminEmail, long userId, String requestedStatus) {
        long adminId = requireAdmin(adminEmail);
        if (requestedStatus == null) throw bad("Trạng thái tài khoản là bắt buộc.");
        String status = requestedStatus.trim().toUpperCase(Locale.ROOT);
        if (!"ACTIVE".equals(status) && !"LOCKED".equals(status)) throw bad("status phải là ACTIVE hoặc LOCKED.");
        UserSummary target = findById(userId, true);
        if (target.id() == adminId && "LOCKED".equals(status)) throw conflict("Không thể tự khóa tài khoản Admin đang đăng nhập.");
        if ("ADMIN".equals(target.role()) && "ACTIVE".equals(target.status()) && "LOCKED".equals(status)) {
            Integer otherAdmins = jdbc.queryForObject("SELECT count(*) FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE' AND id <> ?", Integer.class, userId);
            if (otherAdmins != null && otherAdmins == 0) throw conflict("Không thể khóa Admin đang hoạt động cuối cùng.");
        }
        jdbc.update("UPDATE users SET status = ? WHERE id = ?", status, userId);
        return findById(userId, false);
    }

    private long requireAdmin(String email) {
        if (email == null || email.isBlank() || "anonymousUser".equals(email))
            throw new AdminException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Vui lòng đăng nhập.");
        var ids = jdbc.query("SELECT id FROM users WHERE lower(email) = lower(?) AND status = 'ACTIVE' AND role = 'ADMIN'",
                (rs, row) -> rs.getLong(1), email.trim());
        if (ids.isEmpty()) throw new AdminException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Chỉ Admin mới được thực hiện thao tác này.");
        return ids.get(0);
    }

    private UserSummary findById(long id, boolean lock) {
        String sql = "SELECT id, full_name, email, phone, role, status, created_at FROM users WHERE id = ?" + (lock ? " FOR UPDATE" : "");
        return jdbc.query(sql, (rs, row) -> new UserSummary(rs.getLong("id"), rs.getString("full_name"),
                rs.getString("email"), rs.getString("phone"), rs.getString("role"), rs.getString("status"),
                rs.getObject("created_at", java.time.OffsetDateTime.class)), id).stream().findFirst()
                .orElseThrow(() -> new AdminException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy tài khoản."));
    }

    private String normalizeFilter(String value, String first, String second) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!first.equals(normalized) && !second.equals(normalized)) throw bad("Bộ lọc không hợp lệ.");
        return normalized;
    }

    private static AdminException bad(String message) { return new AdminException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message); }
    private static AdminException conflict(String message) { return new AdminException(HttpStatus.CONFLICT, "CONFLICT", message); }
}
