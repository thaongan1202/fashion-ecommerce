package vn.edu.hcmute.fashion.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() {}
    public record AddCartItem(Long variantId, Integer quantity) {}
    public record UpdateCartItem(Integer quantity) {}
    public record CartItem(Long itemId, Long variantId, String productName, String size, String color,
                           String imageUrl, BigDecimal unitPrice, Integer quantity, Integer availableStock,
                           BigDecimal lineTotal) {}
    public record Cart(List<CartItem> items, BigDecimal subtotal, Integer itemCount) {}
    public record Checkout(Long addressId, String voucherCode) {}
    public record StatusChange(String status, String note) {}
    public record OrderLine(Long id, Long variantId, String productName, String size, String color,
                            Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
    public record OrderSummary(Long id, String orderCode, String status, BigDecimal totalAmount,
                               String recipientName, String phone, String address,
                               OffsetDateTime createdAt, Integer itemCount) {}
    public record OrderDetail(Long id, String orderCode, String status, BigDecimal totalAmount,
                              String recipientName, String phone, String address,
                              OffsetDateTime createdAt, List<OrderLine> items,
                              List<StatusEntry> history) {}
    public record StatusEntry(String status, OffsetDateTime changedAt, String note) {}
}
