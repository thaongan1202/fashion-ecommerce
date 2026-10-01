package vn.edu.hcmute.fashion.order;

import static vn.edu.hcmute.fashion.order.OrderDtos.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final JdbcTemplate jdbc;
    private final CheckoutVoucherPort vouchers;
    private final SecureRandom random = new SecureRandom();
    private static final RowMapper<CartItem> CART_ITEM = (rs, n) -> new CartItem(
            rs.getLong("item_id"), rs.getLong("variant_id"), rs.getString("product_name"),
            rs.getString("size"), rs.getString("color"), rs.getString("image_url"),
            rs.getBigDecimal("price"), rs.getInt("quantity"), rs.getInt("stock_qty"),
            rs.getBigDecimal("price").multiply(BigDecimal.valueOf(rs.getInt("quantity"))));

    public OrderService(JdbcTemplate jdbc, CheckoutVoucherPort vouchers) { this.jdbc = jdbc; this.vouchers = vouchers; }

    private long userId(String email) {
        if (email == null || email.isBlank()) throw new OrderException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Vui lòng đăng nhập.");
        var ids = jdbc.query("select id from users where lower(email)=lower(?) and status='ACTIVE'", (rs, n) -> rs.getLong(1), email);
        if (ids.isEmpty()) throw new OrderException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Tài khoản không hợp lệ hoặc đã bị khóa.");
        return ids.get(0);
    }

    private long requireAdmin(String email) {
        long id = userId(email);
        String role = jdbc.queryForObject("select role from users where id=?", String.class, id);
        if (!"ADMIN".equals(role)) throw new OrderException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Bạn không có quyền thực hiện thao tác này.");
        return id;
    }

    private long cartId(long userId) {
        jdbc.update("insert into carts(user_id) values (?) on conflict (user_id) do nothing", userId);
        return jdbc.queryForObject("select id from carts where user_id=?", Long.class, userId);
    }

    @Transactional(readOnly = true)
    public Cart getCart(String email) {
        long uid = userId(email);
        var found = jdbc.query("select id from carts where user_id=?", (rs,n)->rs.getLong(1), uid);
        if (found.isEmpty()) return new Cart(List.of(), BigDecimal.ZERO, 0);
        List<CartItem> items = jdbc.query("""
                select ci.id item_id, pv.id variant_id, p.name product_name, pv.size, pv.color,
                       pi.image_url, pv.price, ci.quantity, pv.stock_qty
                from cart_items ci join product_variants pv on pv.id=ci.variant_id
                join products p on p.id=pv.product_id
                left join lateral (select image_url from product_images where product_id=p.id order by is_primary desc,id limit 1) pi on true
                where ci.cart_id=? order by ci.id
                """, CART_ITEM, found.get(0));
        BigDecimal subtotal = items.stream().map(CartItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int count = items.stream().mapToInt(CartItem::quantity).sum();
        return new Cart(items, subtotal, count);
    }

    @Transactional
    public Cart addItem(String email, AddCartItem request) {
        if (request == null || request.variantId() == null || request.quantity() == null || request.quantity() < 1)
            throw bad("INVALID_CART_ITEM", "Biến thể và số lượng hợp lệ là bắt buộc.");
        long cartId = cartId(userId(email));
        var stock = jdbc.query("select pv.stock_qty,p.status from product_variants pv join products p on p.id=pv.product_id where pv.id=?", (rs,n)->new String[]{rs.getString(2), Integer.toString(rs.getInt(1))}, request.variantId());
        if (stock.isEmpty() || !"ACTIVE".equals(stock.get(0)[0])) throw missing("Không tìm thấy biến thể sản phẩm đang bán.");
        int available = Integer.parseInt(stock.get(0)[1]);
        var existing = jdbc.query("select id,quantity from cart_items where cart_id=? and variant_id=?", (rs,n)->new int[]{rs.getInt(1),rs.getInt(2)}, cartId, request.variantId());
        int quantity = request.quantity() + (existing.isEmpty() ? 0 : existing.get(0)[1]);
        if (quantity > available) throw conflict("INSUFFICIENT_STOCK", "Số lượng trong giỏ vượt quá tồn kho hiện có.");
        if (existing.isEmpty()) jdbc.update("insert into cart_items(cart_id,variant_id,quantity) values(?,?,?)", cartId, request.variantId(), quantity);
        else jdbc.update("update cart_items set quantity=? where id=?", quantity, existing.get(0)[0]);
        return getCart(email);
    }

    @Transactional
    public Cart updateItem(String email, long itemId, UpdateCartItem request) {
        if (request == null || request.quantity() == null || request.quantity() < 1) throw bad("INVALID_QUANTITY", "Số lượng phải lớn hơn 0.");
        long uid = userId(email);
        var rows = jdbc.query("select ci.variant_id,pv.stock_qty from cart_items ci join carts c on c.id=ci.cart_id join product_variants pv on pv.id=ci.variant_id where ci.id=? and c.user_id=?", (rs,n)->new int[]{rs.getInt(1),rs.getInt(2)}, itemId, uid);
        if (rows.isEmpty()) throw missing("Không tìm thấy sản phẩm trong giỏ.");
        if (request.quantity() > rows.get(0)[1]) throw conflict("INSUFFICIENT_STOCK", "Số lượng vượt quá tồn kho hiện có.");
        jdbc.update("update cart_items set quantity=? where id=?", request.quantity(), itemId);
        return getCart(email);
    }

    @Transactional
    public Cart removeItem(String email, long itemId) {
        int deleted = jdbc.update("delete from cart_items ci using carts c where ci.cart_id=c.id and ci.id=? and c.user_id=?", itemId, userId(email));
        if (deleted == 0) throw missing("Không tìm thấy sản phẩm trong giỏ.");
        return getCart(email);
    }

    @Transactional
    public OrderDetail checkout(String email, Checkout request) {
        long uid = userId(email);
        if (request == null || request.addressId() == null) throw bad("ADDRESS_REQUIRED", "Vui lòng chọn địa chỉ giao hàng.");
        var address = jdbc.query("select recipient_name,phone,address_line from addresses where id=? and user_id=?", (rs,n)->new String[]{rs.getString(1),rs.getString(2),rs.getString(3)}, request.addressId(), uid);
        if (address.isEmpty()) throw bad("INVALID_ADDRESS", "Địa chỉ không tồn tại hoặc không thuộc tài khoản của bạn.");
        long cid = cartId(uid);
        List<CartItem> items = jdbc.query("""
                select ci.id item_id,pv.id variant_id,p.name product_name,pv.size,pv.color,pi.image_url,pv.price,ci.quantity,pv.stock_qty
                from cart_items ci join product_variants pv on pv.id=ci.variant_id join products p on p.id=pv.product_id
                left join lateral (select image_url from product_images where product_id=p.id order by is_primary desc,id limit 1) pi on true
                where ci.cart_id=? order by pv.id for update of pv
                """, CART_ITEM, cid);
        if (items.isEmpty()) throw bad("EMPTY_CART", "Giỏ hàng đang trống.");
        Integer inactiveCount = jdbc.queryForObject("select count(*) from cart_items ci join product_variants pv on pv.id=ci.variant_id join products p on p.id=pv.product_id where ci.cart_id=? and p.status<>'ACTIVE'", Integer.class, cid);
        if (inactiveCount != null && inactiveCount > 0) throw conflict("PRODUCT_UNAVAILABLE", "Một sản phẩm trong giỏ hiện không còn được bán.");
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : items) {
            if (item.quantity() > item.availableStock()) throw conflict("INSUFFICIENT_STOCK", "Một sản phẩm trong giỏ không còn đủ tồn kho.");
            subtotal = subtotal.add(item.lineTotal());
        }
        String code = request.voucherCode() == null || request.voucherCode().isBlank() ? null : request.voucherCode().trim().toUpperCase(Locale.ROOT);
        BigDecimal discount = code == null ? BigDecimal.ZERO : vouchers.redeem(code, subtotal);
        if (discount == null || discount.signum() < 0 || discount.compareTo(subtotal) > 0) throw conflict("INVALID_DISCOUNT", "Mức giảm giá không hợp lệ.");
        BigDecimal total = subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        String orderCode = newOrderCode();
        Long voucherId = null;
        if (code != null) {
            var ids = jdbc.query("select id from vouchers where upper(code)=?", (rs,n)->rs.getLong(1), code);
            if (ids.isEmpty()) throw conflict("INVALID_VOUCHER", "Voucher không tồn tại.");
            voucherId = ids.get(0);
        }
        jdbc.update("insert into orders(order_code,user_id,voucher_id,total_amount,status,payment_method,shipping_recipient_name,shipping_phone,shipping_address_line) values(?,?,?,?,'PENDING','COD',?,?,?)",
                orderCode, uid, voucherId, total, address.get(0)[0], address.get(0)[1], address.get(0)[2]);
        long oid = jdbc.queryForObject("select id from orders where order_code=?", Long.class, orderCode);
        for (CartItem item : items) {
            int changed = jdbc.update("update product_variants set stock_qty=stock_qty-? where id=? and stock_qty>=?", item.quantity(), item.variantId(), item.quantity());
            if (changed != 1) throw conflict("INSUFFICIENT_STOCK", "Tồn kho vừa thay đổi; vui lòng kiểm tra lại giỏ hàng.");
            jdbc.update("insert into order_items(order_id,variant_id,product_name_snapshot,size,color,quantity,price_at_purchase) values(?,?,?,?,?,?,?)",
                    oid, item.variantId(), item.productName(), item.size(), item.color(), item.quantity(), item.unitPrice());
        }
        jdbc.update("insert into order_status_history(order_id,status,note) values(?,'PENDING','Đơn hàng được tạo')", oid);
        jdbc.update("delete from cart_items where cart_id=?", cid);
        return getOrderDetail(oid);
    }

    @Transactional(readOnly = true)
    public List<OrderSummary> listOrders(String email, boolean admin) {
        long uid = admin ? requireAdmin(email) : userId(email);
        String sql = "select o.id,o.order_code,o.status,o.total_amount,o.shipping_recipient_name,o.shipping_phone,o.shipping_address_line,o.created_at,(select coalesce(sum(oi.quantity),0) from order_items oi where oi.order_id=o.id) item_count from orders o " + (admin ? "" : "where o.user_id=? ") + "order by o.created_at desc,o.id desc";
        return admin ? jdbc.query(sql, this::mapSummary) : jdbc.query(sql, this::mapSummary, uid);
    }

    @Transactional(readOnly = true)
    public OrderDetail orderDetail(String email, long orderId, boolean admin) {
        long uid = admin ? requireAdmin(email) : userId(email);
        if (!admin && jdbc.query("select id from orders where id=? and user_id=?", (rs,n)->rs.getLong(1), orderId, uid).isEmpty()) throw missing("Không tìm thấy đơn hàng.");
        return getOrderDetail(orderId);
    }

    @Transactional
    public OrderDetail cancel(String email, long orderId) {
        long uid = userId(email);
        var rows = jdbc.query("select status from orders where id=? and user_id=? for update", (rs,n)->rs.getString(1), orderId, uid);
        if (rows.isEmpty()) throw missing("Không tìm thấy đơn hàng.");
        if (!"PENDING".equals(rows.get(0))) throw conflict("INVALID_ORDER_STATE", "Chỉ có thể hủy đơn đang ở trạng thái PENDING.");
        jdbc.update("update orders set status='CANCELLED' where id=?", orderId);
        jdbc.update("insert into order_status_history(order_id,status,note) values(?,'CANCELLED','Khách hàng hủy đơn')", orderId);
        jdbc.update("update product_variants pv set stock_qty=pv.stock_qty+oi.quantity from order_items oi where oi.order_id=? and pv.id=oi.variant_id", orderId);
        return getOrderDetail(orderId);
    }

    @Transactional
    public OrderDetail changeStatus(String email, long orderId, StatusChange change) {
        requireAdmin(email);
        if (change == null || change.status() == null) throw bad("INVALID_STATUS", "Trạng thái mới là bắt buộc.");
        var rows = jdbc.query("select status from orders where id=? for update", (rs,n)->rs.getString(1), orderId);
        if (rows.isEmpty()) throw missing("Không tìm thấy đơn hàng.");
        String from = rows.get(0), to = change.status().toUpperCase(Locale.ROOT);
        if (!isAllowedTransition(from, to)) throw conflict("INVALID_ORDER_TRANSITION", "Không thể chuyển đơn từ " + from + " sang " + to + ".");
        jdbc.update("update orders set status=? where id=?", to, orderId);
        jdbc.update("insert into order_status_history(order_id,status,note) values(?,?,?)", orderId, to, change.note());
        if ("CANCELLED".equals(to)) jdbc.update("update product_variants pv set stock_qty=pv.stock_qty+oi.quantity from order_items oi where oi.order_id=? and pv.id=oi.variant_id", orderId);
        return getOrderDetail(orderId);
    }

    static boolean isAllowedTransition(String from, String to) {
        return switch (from) {
            case "PENDING" -> "PROCESSING".equals(to) || "CANCELLED".equals(to);
            case "PROCESSING" -> "SHIPPING".equals(to) || "CANCELLED".equals(to);
            case "SHIPPING" -> "DELIVERED".equals(to);
            default -> false;
        };
    }

    private OrderDetail getOrderDetail(long orderId) {
        var rows = jdbc.query("select id,order_code,status,total_amount,shipping_recipient_name,shipping_phone,shipping_address_line,created_at from orders where id=?", (rs,n)->new Object[]{rs.getLong(1),rs.getString(2),rs.getString(3),rs.getBigDecimal(4),rs.getString(5),rs.getString(6),rs.getString(7),rs.getObject(8,OffsetDateTime.class)}, orderId);
        if (rows.isEmpty()) throw missing("Không tìm thấy đơn hàng.");
        Object[] r = rows.get(0);
        List<OrderLine> lines = jdbc.query("select id,variant_id,product_name_snapshot,size,color,quantity,price_at_purchase from order_items where order_id=? order by id", (rs,n)->new OrderLine(rs.getLong(1),rs.getLong(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getInt(6),rs.getBigDecimal(7),rs.getBigDecimal(7).multiply(BigDecimal.valueOf(rs.getInt(6)))), orderId);
        List<StatusEntry> history = jdbc.query("select status,changed_at,note from order_status_history where order_id=? order by changed_at,id", (rs,n)->new StatusEntry(rs.getString(1),rs.getObject(2,OffsetDateTime.class),rs.getString(3)), orderId);
        return new OrderDetail((Long)r[0],(String)r[1],(String)r[2],(BigDecimal)r[3],(String)r[4],(String)r[5],(String)r[6],(OffsetDateTime)r[7],lines,history);
    }
    private OrderSummary mapSummary(java.sql.ResultSet rs, int n) throws java.sql.SQLException {
        return new OrderSummary(rs.getLong("id"),rs.getString("order_code"),rs.getString("status"),rs.getBigDecimal("total_amount"),rs.getString("shipping_recipient_name"),rs.getString("shipping_phone"),rs.getString("shipping_address_line"),rs.getObject("created_at",OffsetDateTime.class),rs.getInt("item_count"));
    }
    private String newOrderCode() { byte[] b=new byte[6]; random.nextBytes(b); return "ORD-"+Long.toString(System.currentTimeMillis(),36).toUpperCase(Locale.ROOT)+java.util.HexFormat.of().formatHex(b).toUpperCase(Locale.ROOT); }
    private static OrderException bad(String code,String message) { return new OrderException(HttpStatus.BAD_REQUEST,code,message); }
    private static OrderException missing(String message) { return new OrderException(HttpStatus.NOT_FOUND,"NOT_FOUND",message); }
    private static OrderException conflict(String code,String message) { return new OrderException(HttpStatus.CONFLICT,code,message); }
}
