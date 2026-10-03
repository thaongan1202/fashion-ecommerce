package vn.edu.hcmute.fashion.voucher;

import static vn.edu.hcmute.fashion.voucher.VoucherDtos.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Voucher rules shared by the customer preview API and the order checkout transaction. */
@Service
public class VoucherService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final String SELECT_COLUMNS = "id, code, discount_type, discount_value, min_order_value, expiry_date, usage_limit, used_count, is_active, created_at";
    private static final RowMapper<VoucherResponse> VOUCHER_MAPPER = (rs, row) -> new VoucherResponse(
            rs.getLong("id"), rs.getString("code"), DiscountType.from(rs.getString("discount_type")),
            rs.getBigDecimal("discount_value"), rs.getBigDecimal("min_order_value"),
            rs.getObject("expiry_date", LocalDate.class), rs.getInt("usage_limit"), rs.getInt("used_count"),
            rs.getBoolean("is_active"), rs.getObject("created_at", OffsetDateTime.class));

    private final JdbcTemplate jdbc;

    public VoucherService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional(readOnly = true)
    public ApplyResponse preview(String email, String rawCode) {
        long userId = requireCustomer(email);
        BigDecimal subtotal = jdbc.queryForObject("""
                SELECT COALESCE(SUM(pv.price * ci.quantity), 0)
                FROM carts c LEFT JOIN cart_items ci ON ci.cart_id = c.id
                LEFT JOIN product_variants pv ON pv.id = ci.variant_id
                WHERE c.user_id = ?
                """, BigDecimal.class, userId);
        if (subtotal == null || subtotal.signum() <= 0) throw invalid("Giỏ hàng đang trống.");
        return preview(rawCode, subtotal);
    }

    @Transactional(readOnly = true)
    public ApplyResponse preview(String email, String rawCode, BigDecimal subtotal) {
        requireCustomer(email);
        return preview(rawCode, subtotal);
    }

    @Transactional(readOnly = true)
    public ApplyResponse preview(String rawCode, BigDecimal subtotal) {
        requireSubtotal(subtotal);
        VoucherResponse voucher = findByCode(normalizeCode(rawCode), false);
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        ensureUsable(voucher, subtotal, today);
        BigDecimal discount = calculateDiscount(voucher.discountType().value(), voucher.discountValue(), subtotal);
        return new ApplyResponse(true, voucher.code(), discount, subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP), "Đã áp dụng mã giảm giá.");
    }

    /** Called from the order's existing @Transactional method; REQUIRED joins that transaction. */
    @Transactional
    public BigDecimal redeem(String rawCode, BigDecimal subtotal) {
        requireSubtotal(subtotal);
        String code = normalizeCode(rawCode);
        VoucherResponse voucher = findByCode(code, true);
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        ensureUsable(voucher, subtotal, today);
        int changed = jdbc.update("UPDATE vouchers SET used_count = used_count + 1 WHERE id = ? AND is_active = TRUE AND used_count < usage_limit AND expiry_date >= ?",
                voucher.id(), today);
        if (changed != 1) throw invalid("Voucher vừa hết lượt hoặc hết hạn; vui lòng thử lại.");
        return calculateDiscount(voucher.discountType().value(), voucher.discountValue(), subtotal);
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse> list(String adminEmail) {
        requireAdmin(adminEmail);
        return jdbc.query("SELECT " + SELECT_COLUMNS + " FROM vouchers ORDER BY created_at DESC, id DESC", VOUCHER_MAPPER);
    }

    @Transactional(readOnly = true)
    public VoucherResponse get(String adminEmail, long id) {
        requireAdmin(adminEmail);
        return findById(id);
    }

    @Transactional
    public VoucherResponse create(String adminEmail, VoucherRequest request) {
        requireAdmin(adminEmail);
        validateRequest(request, 0);
        String code = normalizeCode(request.code());
        if (existsCode(code, null)) throw conflict("Mã voucher đã tồn tại.");
        try {
            Long id = jdbc.queryForObject("""
                    INSERT INTO vouchers (code, discount_type, discount_value, min_order_value, expiry_date, usage_limit, is_active)
                    VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id
                    """, Long.class, code, request.discountType().value(), request.discountValue(),
                    request.minOrderValue(), request.expiryDate(), request.usageLimit(), request.active());
            return findById(id);
        } catch (DataIntegrityViolationException exception) {
            throw conflict("Mã voucher đã tồn tại hoặc dữ liệu không hợp lệ.");
        }
    }

    @Transactional
    public VoucherResponse update(String adminEmail, long id, VoucherRequest request) {
        requireAdmin(adminEmail);
        VoucherResponse current = findById(id);
        validateRequest(request, current.usedCount());
        String code = normalizeCode(request.code());
        if (existsCode(code, id)) throw conflict("Mã voucher đã tồn tại.");
        try {
            jdbc.update("""
                    UPDATE vouchers SET code = ?, discount_type = ?, discount_value = ?, min_order_value = ?,
                        expiry_date = ?, usage_limit = ?, is_active = ? WHERE id = ?
                    """, code, request.discountType().value(), request.discountValue(), request.minOrderValue(),
                    request.expiryDate(), request.usageLimit(), request.active(), id);
            return findById(id);
        } catch (DataIntegrityViolationException exception) {
            throw conflict("Dữ liệu voucher không hợp lệ.");
        }
    }

    @Transactional
    public void delete(String adminEmail, long id) {
        requireAdmin(adminEmail);
        VoucherResponse voucher = findById(id);
        Boolean referenced = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM orders WHERE voucher_id = ?)", Boolean.class, id);
        if (voucher.usedCount() > 0 || Boolean.TRUE.equals(referenced)) {
            jdbc.update("UPDATE vouchers SET is_active = FALSE WHERE id = ?", id);
            return;
        }
        jdbc.update("DELETE FROM vouchers WHERE id = ?", id);
    }

    private VoucherResponse findById(long id) {
        return jdbc.query("SELECT " + SELECT_COLUMNS + " FROM vouchers WHERE id = ?", VOUCHER_MAPPER, id)
                .stream().findFirst().orElseThrow(() -> notFound("Không tìm thấy voucher."));
    }

    private VoucherResponse findByCode(String code, boolean lock) {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM vouchers WHERE upper(code) = ?" + (lock ? " FOR UPDATE" : "");
        return jdbc.query(sql, VOUCHER_MAPPER, code).stream().findFirst()
                .orElseThrow(() -> invalid("Mã voucher không tồn tại hoặc không còn sử dụng được."));
    }

    private boolean existsCode(String code, Long exceptId) {
        if (exceptId == null) return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM vouchers WHERE upper(code) = ?)", Boolean.class, code));
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM vouchers WHERE upper(code) = ? AND id <> ?)", Boolean.class, code, exceptId));
    }

    static void ensureUsable(VoucherResponse voucher, BigDecimal subtotal, LocalDate today) {
        if (!voucher.active()) throw invalid("Voucher đã bị tắt.");
        if (voucher.expiryDate().isBefore(today)) throw invalid("Voucher đã hết hạn.");
        if (voucher.usedCount() >= voucher.usageLimit()) throw invalid("Voucher đã hết lượt sử dụng.");
        if (subtotal.compareTo(voucher.minOrderValue()) < 0) throw invalid("Đơn hàng chưa đạt giá trị tối thiểu của voucher.");
    }

    static BigDecimal calculateDiscount(String type, BigDecimal value, BigDecimal subtotal) {
        BigDecimal discount = switch (type) {
            case "PERCENT" -> subtotal.multiply(value).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            case "FIXED" -> value;
            default -> throw invalid("Loại voucher không hợp lệ.");
        };
        if (discount.compareTo(subtotal) > 0) discount = subtotal;
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateRequest(VoucherRequest request, int usedCount) {
        if (request == null || request.discountType() == null || request.discountValue() == null
                || request.minOrderValue() == null || request.expiryDate() == null || request.usageLimit() == null)
            throw bad("Thiếu thông tin voucher bắt buộc.");
        String type = request.discountType().value();
        if (!"PERCENT".equals(type) && !"FIXED".equals(type)) throw bad("discountType phải là PERCENT hoặc FIXED.");
        if (request.discountValue().signum() <= 0 || request.minOrderValue().signum() < 0)
            throw bad("Giá trị giảm phải lớn hơn 0 và giá trị đơn tối thiểu không âm.");
        if ("PERCENT".equals(type) && request.discountValue().compareTo(new BigDecimal("100")) > 0)
            throw bad("Voucher phần trăm không được vượt quá 100%.");
        if (request.usageLimit() < 1 || request.usageLimit() < usedCount)
            throw bad("Giới hạn lượt không được nhỏ hơn số lượt đã sử dụng.");
        if (request.expiryDate().isBefore(LocalDate.now(BUSINESS_ZONE)))
            throw bad("Ngày hết hạn không được nằm trong quá khứ.");
    }

    private void requireSubtotal(BigDecimal subtotal) {
        if (subtotal == null || subtotal.signum() < 0) throw bad("Tổng tiền đơn hàng không hợp lệ.");
    }

    private String normalizeCode(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) throw bad("Vui lòng nhập mã voucher.");
        String code = rawCode.trim().toUpperCase(Locale.ROOT);
        if (code.length() > 60 || !code.matches("[A-Z0-9_-]+")) throw bad("Mã voucher chỉ gồm chữ, số, dấu gạch ngang hoặc gạch dưới.");
        return code;
    }

    private long requireCustomer(String email) {
        if (email == null || email.isBlank() || "anonymousUser".equals(email)) throw unauthorized();
        var rows = jdbc.query("SELECT id FROM users WHERE lower(email) = lower(?) AND status = 'ACTIVE' AND role = 'CUSTOMER'",
                (rs, row) -> rs.getLong(1), email.trim());
        if (rows.isEmpty()) throw unauthorized();
        return rows.get(0);
    }

    private long requireAdmin(String email) {
        if (email == null || email.isBlank() || "anonymousUser".equals(email)) throw unauthorized();
        var rows = jdbc.query("SELECT id FROM users WHERE lower(email) = lower(?) AND status = 'ACTIVE' AND role = 'ADMIN'",
                (rs, row) -> rs.getLong(1), email.trim());
        if (rows.isEmpty()) throw new VoucherException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Bạn không có quyền quản trị.");
        return rows.get(0);
    }

    private static VoucherException unauthorized() { return new VoucherException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Vui lòng đăng nhập bằng tài khoản hợp lệ."); }
    private static VoucherException invalid(String message) { return new VoucherException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_VOUCHER", message); }
    private static VoucherException bad(String message) { return new VoucherException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message); }
    private static VoucherException conflict(String message) { return new VoucherException(HttpStatus.CONFLICT, "CONFLICT", message); }
    private static VoucherException notFound(String message) { return new VoucherException(HttpStatus.NOT_FOUND, "NOT_FOUND", message); }
}
