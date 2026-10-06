package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.request.order.CreateOrderRequest;

import com.utephonehub.backend.dto.request.order.OrderItemRequest;
import com.utephonehub.backend.dto.response.order.CreateOrderResponse;
import com.utephonehub.backend.dto.response.order.OrderResponse;
import com.utephonehub.backend.dto.request.payment.CreatePaymentRequest;
import com.utephonehub.backend.dto.response.payment.VNPayPaymentResponse;
import com.utephonehub.backend.entity.Cart;
import com.utephonehub.backend.entity.CartItem;
import com.utephonehub.backend.entity.Order;
import com.utephonehub.backend.entity.OrderItem;
import com.utephonehub.backend.entity.Payment;
import com.utephonehub.backend.entity.Product;
import com.utephonehub.backend.entity.ProductTemplate;
import com.utephonehub.backend.entity.Promotion;
import com.utephonehub.backend.entity.User;
import com.utephonehub.backend.entity.OrderReturn;
import com.utephonehub.backend.enums.OrderStatus;
import com.utephonehub.backend.enums.PaymentMethod;
import com.utephonehub.backend.enums.PaymentStatus;
import com.utephonehub.backend.exception.BadRequestException;
import com.utephonehub.backend.exception.ForbiddenException;
import com.utephonehub.backend.exception.ResourceNotFoundException;
import com.utephonehub.backend.mapper.OrderMapper;
import com.utephonehub.backend.entity.OrderStatusHistory;
import com.utephonehub.backend.enums.EPromotionStatus;
import com.utephonehub.backend.repository.OrderStatusHistoryRepository;
import com.utephonehub.backend.repository.CartItemRepository;
import com.utephonehub.backend.repository.CartRepository;
import com.utephonehub.backend.repository.OrderItemRepository;
import com.utephonehub.backend.repository.OrderRepository;
import com.utephonehub.backend.repository.OrderReturnRepository;
import com.utephonehub.backend.repository.PaymentRepository;
import com.utephonehub.backend.repository.ProductRepository;
import com.utephonehub.backend.repository.PromotionRepository;
import com.utephonehub.backend.repository.UserRepository;
import com.utephonehub.backend.service.IEmailService;
import com.utephonehub.backend.service.IOrderService;
import com.utephonehub.backend.service.IVNPayService;
import com.utephonehub.backend.service.InventoryService;
import com.utephonehub.backend.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements IOrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final PaymentRepository paymentRepository;
    private final OrderMapper orderMapper;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final IVNPayService vnPayService;
    private final SecurityUtils securityUtils;
    private final IEmailService emailService;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final InventoryService inventoryService;
    private final OrderReturnRepository orderReturnRepository;
    
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) {
        
        // 1. Tìm Order kèm items
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", orderId);
                    return new ResourceNotFoundException("Đơn hàng không tồn tại");
                });
        
        // 2. Kiểm tra quyền sở hữu
        if (!order.getUser().getId().equals(userId)) {
            log.warn("User {} tried to access order {} owned by user {}", 
                    userId, orderId, order.getUser().getId());
            throw new ForbiddenException("Bạn không có quyền xem đơn hàng này");
        }
        
        // 3. Convert sang DTO bằng Mapper
        log.info("Get order {} by user {}", orderId, userId);
        return toCustomerOrderResponse(order); 
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public CreateOrderResponse createOrder(CreateOrderRequest request, Long userId) {
        return createOrder(request, userId, null);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public CreateOrderResponse createOrder(CreateOrderRequest request, Long userId, HttpServletRequest servletRequest) {
        log.info("Creating order for user: {}", userId);
        
        // 1. Validate user tồn tại
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        
        // 2. Validate danh sách sản phẩm
        // distinct(): cùng 1 sản phẩm có thể xuất hiện nhiều dòng (khác màu/size)
        List<Long> productIds = request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Product> products = productRepository.findAllByIdIn(productIds);
        
        if (products.size() != productIds.size()) {
            throw new BadRequestException("Một số sản phẩm không tồn tại");
        }
        
        // 3. Map product theo ID để dễ tìm kiếm
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        // 4. Validate tồn kho và tính tổng tiền
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemRequest> validatedItems = new ArrayList<>();
        
        // Tổng số lượng theo đúng biến thể màu/size
        Map<String, Integer> requestedByVariant = new java.util.HashMap<>();
        for (OrderItemRequest item : request.getItems()) {
            if (item.getQuantity() == null || item.getQuantity() < 1) {
                throw new BadRequestException("Số lượng không được âm và phải lớn hơn hoặc bằng 1");
            }
            String variantKey = item.getProductId() + "|" + normalizeVariant(item.getColor()) + "|" + normalizeVariant(item.getSize());
            requestedByVariant.merge(variantKey, item.getQuantity(), Integer::sum);
        }

        for (OrderItemRequest item : request.getItems()) {
            Product product = productMap.get(item.getProductId());
            String variantKey = item.getProductId() + "|" + normalizeVariant(item.getColor()) + "|" + normalizeVariant(item.getSize());
            int requested = requestedByVariant.get(variantKey);
            int variantStock = inventoryService.availableStock(product, item.getColor(), item.getSize());

            if (requested > variantStock) {
                throw new BadRequestException(
                    String.format("Sản phẩm '%s' chỉ còn %d trong kho. Không thể đặt số lượng lớn hơn hàng còn.",
                        product.getName(), variantStock)
                );
            }

            BigDecimal price = inventoryService.priceOf(product, item.getColor(), item.getSize());
            
            // Tính tổng tiền
            BigDecimal itemTotal = price.multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            
            validatedItems.add(item);
        }
        
        // 5. Áp dụng promotion nếu có (2 loại: DISCOUNT/VOUCHER và FREESHIP)
        Promotion promotion = null;
        Promotion freeshippingPromotion = null;
        LocalDateTime now = LocalDateTime.now();
        
        // Xử lý promotion (DISCOUNT/VOUCHER)
        if (request.getPromotionId() != null) {
            promotion = promotionRepository.findById(String.valueOf(request.getPromotionId()))
                    .orElseThrow(() -> new BadRequestException("Mã khuyến mãi không tồn tại"));
            
            // Validate trạng thái hoạt động
            if (promotion.getStatus() != EPromotionStatus.ACTIVE) {
                throw new BadRequestException("Mã khuyến mãi không còn hoạt động hoặc đã bị vô hiệu hóa");
            }
            
            // Validate thời gian hiệu lực
            if (promotion.getEffectiveDate() != null && now.isBefore(promotion.getEffectiveDate())) {
                throw new BadRequestException("Mã khuyến mãi chưa đến thời gian có hiệu lực");
            }
            if (promotion.getExpirationDate() != null && now.isAfter(promotion.getExpirationDate())) {
                throw new BadRequestException("Mã khuyến mãi đã hết hạn sử dụng");
            }
            
            // Validate giá trị đơn hàng tối thiểu
            if (promotion.getMinValueToBeApplied() != null 
                    && totalAmount.compareTo(BigDecimal.valueOf(promotion.getMinValueToBeApplied())) < 0) {
                throw new BadRequestException(
                    String.format("Đơn hàng chưa đạt giá trị tối thiểu %,.0f VNĐ để áp dụng mã khuyến mãi", 
                        promotion.getMinValueToBeApplied())
                );
            }
            
            // Tính số tiền được giảm giá
            BigDecimal discountAmount = BigDecimal.ZERO;
            if (promotion.getFixedAmount() != null && promotion.getFixedAmount() > 0) {
                discountAmount = BigDecimal.valueOf(promotion.getFixedAmount());
            } else if (promotion.getPercentDiscount() != null && promotion.getPercentDiscount() > 0) {
                discountAmount = totalAmount.multiply(BigDecimal.valueOf(promotion.getPercentDiscount() / 100.0));
                if (promotion.getMaxDiscount() != null && promotion.getMaxDiscount() > 0) {
                    BigDecimal maxDisc = BigDecimal.valueOf(promotion.getMaxDiscount());
                    if (discountAmount.compareTo(maxDisc) > 0) {
                        discountAmount = maxDisc;
                    }
                }
            }
            
            // Trừ số tiền giảm giá vào tổng đơn hàng
            totalAmount = totalAmount.subtract(discountAmount);
            if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
                totalAmount = BigDecimal.ZERO;
            }
            log.info("Applied promotion {}: discount amount = {}, new total = {}", 
                    promotion.getId(), discountAmount, totalAmount);
        }
        
        // Xử lý freeship promotion (FREESHIP)
        if (request.getFreeshippingPromotionId() != null) {
            freeshippingPromotion = promotionRepository.findById(String.valueOf(request.getFreeshippingPromotionId()))
                    .orElseThrow(() -> new BadRequestException("Mã miễn phí vận chuyển không tồn tại"));
            
            if (freeshippingPromotion.getStatus() != EPromotionStatus.ACTIVE) {
                throw new BadRequestException("Mã miễn phí vận chuyển không còn hoạt động");
            }
            if (freeshippingPromotion.getEffectiveDate() != null && now.isBefore(freeshippingPromotion.getEffectiveDate())) {
                throw new BadRequestException("Mã miễn phí vận chuyển chưa đến thời gian áp dụng");
            }
            if (freeshippingPromotion.getExpirationDate() != null && now.isAfter(freeshippingPromotion.getExpirationDate())) {
                throw new BadRequestException("Mã miễn phí vận chuyển đã hết hạn sử dụng");
            }
            if (freeshippingPromotion.getMinValueToBeApplied() != null 
                    && totalAmount.compareTo(BigDecimal.valueOf(freeshippingPromotion.getMinValueToBeApplied())) < 0) {
                throw new BadRequestException(
                    String.format("Đơn hàng chưa đạt giá trị tối thiểu %,.0f VNĐ để áp dụng miễn phí vận chuyển", 
                        freeshippingPromotion.getMinValueToBeApplied())
                );
            }
            log.info("Applied freeshipping promotion {}", freeshippingPromotion.getId());
        }
        
        // 6. Tạo orderCode unique
        String orderCode = generateUniqueOrderCode();
        
        // Đơn mới luôn chờ admin xác nhận. Tồn kho chỉ trừ khi đã thanh toán.
        OrderStatus initialStatus = OrderStatus.PENDING;
        
        // 8. Tạo Order entity
        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .email(request.getEmail())
                .recipientName(request.getRecipientName())
                .phoneNumber(request.getPhoneNumber())
                .shippingAddress(request.getShippingAddress())
                .shippingFee(request.getShippingFee())
                .shippingUnit(request.getShippingUnit())
                .note(request.getNote())
                .status(initialStatus)
                .paymentMethod(request.getPaymentMethod())
                .totalAmount(totalAmount)
                .promotion(promotion)
                .freeshippingPromotion(freeshippingPromotion)
                .stockDeducted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // 9. Lưu Order
        order = orderRepository.save(order);
        log.info("Created order: {}", orderCode);
        
        // 9. Tạo OrderItems
        for (OrderItemRequest itemReq : validatedItems) {
            Product product = productMap.get(itemReq.getProductId());
            
            // Get price of the selected color/size
            BigDecimal price = inventoryService.priceOf(product, itemReq.getColor(), itemReq.getSize());
            
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .color(normalizeVariant(itemReq.getColor()))
                    .size(normalizeVariant(itemReq.getSize()))
                    .price(price)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            orderItemRepository.save(orderItem);
        }
        // 10. Tạo Payment record cho COD/Bank Transfer (VNPay sẽ tạo trong callback)
        if (request.getPaymentMethod() != PaymentMethod.VNPAY) {
            for (OrderItemRequest itemReq : validatedItems) {
                Product product = productMap.get(itemReq.getProductId());
                inventoryService.deduct(product, itemReq.getColor(), itemReq.getSize(), itemReq.getQuantity());
            }
            order.setStockDeducted(true);
            orderRepository.save(order);
            
            // 10.2. Tạo Payment record với status SUCCESS (đã thanh toán)
            Payment payment = Payment.builder()
                    .order(order)
                    .provider(null)  // COD/Bank Transfer không có provider
                    .transactionId(null)  // Không có transaction ID
                    .amount(totalAmount)
                    .status(PaymentStatus.SUCCESS)  // Thanh toán ngay = SUCCESS
                    .note("Thanh toán " + request.getPaymentMethod().name())
                    .reconciled(false)
                    .build();
            paymentRepository.save(payment);
            log.info("Created payment record for COD/Bank Transfer: orderId={}, amount={}", 
                    order.getId(), totalAmount);
            
            // Send order confirmation email for COD/Bank Transfer (payment already successful)
            try {
                String paymentMethodName = request.getPaymentMethod() == PaymentMethod.COD 
                    ? "Thanh toán khi nhận hàng" 
                    : "Chuyển khoản ngân hàng";
                
                emailService.sendOrderPaymentSuccessEmail(
                    order.getEmail(),
                    order.getOrderCode(),
                    order.getTotalAmount(),
                    order.getRecipientName(),
                    paymentMethodName
                );
                log.info("Order confirmation email sent for COD/Bank Transfer order: {}", order.getOrderCode());
            } catch (Exception e) {
                log.error("Failed to send order confirmation email for order {}: {}", 
                         order.getOrderCode(), e.getMessage());
                // Không throw exception để không ảnh hưởng order creation
            }
        }

        // 11.1. AF2 – After successfully creating the order, automatically remove
        // the corresponding CartItems in the cart (do not delete the entire cart).
        try {
            // Chỉ xóa đúng dòng giỏ (sản phẩm + màu + size) đã được đặt, giữ lại các biến thể khác
            cartRepository.findByUserIdWithItems(userId).ifPresent(cart -> {
                List<CartItem> toRemove = cart.getItemsInternal().stream()
                        .filter(item -> validatedItems.stream().anyMatch(ordered ->
                                ordered.getProductId().equals(item.getProduct().getId())
                                        && sameVariant(item.getColor(), ordered.getColor())
                                        && sameVariant(item.getSize(), ordered.getSize())))
                        .toList();

                if (!toRemove.isEmpty()) {
                    log.info("Clearing {} ordered items from cart for user {} after order {}",
                            toRemove.size(), userId, orderCode);

                    toRemove.forEach(cart::removeItem);
                    cartItemRepository.deleteAll(toRemove);
                    cartRepository.save(cart);
                }
            });
        } catch (Exception ex) {
            // Don't let cart clearing errors fail the order
            log.error("Failed to clear ordered items from cart for user {} after order {}", userId, orderCode, ex);
        }
        
        // 12. Create response
        CreateOrderResponse response = CreateOrderResponse.builder()
                .orderId(order.getId())
                .orderCode(orderCode)
                .status(initialStatus)
                .paymentMethod(request.getPaymentMethod())
                .totalAmount(totalAmount)
                .createdAt(order.getCreatedAt())
                .build();
        
        // 13. If payment method is VNPay, add instruction message
        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {
            response.setMessage("Đơn hàng đã tạo. Đang chuyển hướng thanh toán VNPay...");
            
            // Tích hợp VNPay payment URL
            try {
                CreatePaymentRequest paymentRequest = CreatePaymentRequest.builder()
                        .orderId(order.getId())
                        .amount(totalAmount.longValue())
                        .orderInfo("Thanh toan don hang " + orderCode)
                        .locale("vn")
                        .build();

                // Lấy IP client phục vụ VNPay; nếu servletRequest null thì fallback về 127.0.0.1
                String ipAddress;
                if (servletRequest != null) {
                    ipAddress = securityUtils.getClientIp(servletRequest);
                } else {
                    log.warn("HttpServletRequest is null when creating VNPay URL for order {}. Using fallback IP 127.0.0.1", orderCode);
                    ipAddress = "127.0.0.1";
                }

                VNPayPaymentResponse paymentResponse = vnPayService.createPaymentUrl(paymentRequest, ipAddress);
                response.setPaymentUrl(paymentResponse.getPaymentUrl());
                log.info("Generated VNPay URL for order {}: {}", orderCode, paymentResponse.getPaymentUrl());
            } catch (Exception e) {
                log.error("Failed to generate VNPay URL for order {}", orderCode, e);
                response.setMessage("Đơn hàng đã tạo nhưng lỗi tạo link thanh toán. Vui lòng thử lại trong lịch sử đơn hàng.");
            }
        } else {
            response.setMessage("Đơn hàng đã được tạo thành công!");
        }
        
        log.info("Order created successfully: {}", orderCode);
        return response;
    }
    
    /** Chuẩn hóa màu/size: rỗng => null, bỏ khoảng trắng thừa. */
    private static String normalizeVariant(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    /** So khớp màu/size null-safe, không phân biệt hoa/thường. */
    private static boolean sameVariant(String a, String b) {
        String na = normalizeVariant(a);
        String nb = normalizeVariant(b);
        if (na == null || nb == null) return na == null && nb == null;
        return na.equalsIgnoreCase(nb);
    }

    /**
     * Generate unique order code
     * Format: ORD_YYMMDDHHMMSS (20 chars max)
     * Example: ORD_251207093853
     */
    private String generateUniqueOrderCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String orderCode = "ORD_" + timestamp;
        
        // Kiểm tra trùng lặp (rất hiếm khi xảy ra)
        int counter = 1;
        String uniqueCode = orderCode;
        while (orderRepository.existsByOrderCode(uniqueCode)) {
            uniqueCode = orderCode + counter;
            counter++;
        }
        
        return uniqueCode;
    }
    
    
    //Xem đơn hàng của chính mình
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Long userId) {
        log. info("Getting orders for user: {}", userId);
        
        User user = userRepository. findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        
       
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        
        log.info("Found {} orders for user {}", orders.size(), userId);
        
        return orders.stream()
                .map(this::toCustomerOrderResponse)
                .collect(Collectors. toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrdersWithPagination(Long userId, Pageable pageable) {
        log.info("Getting orders with pagination for user: {}, page: {}, size: {}", 
                userId, pageable.getPageNumber(), pageable.getPageSize());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        
      
        Page<Order> orderPage = orderRepository. findByUserOrderByCreatedAtDesc(user, pageable);
        
        log.info("Found {} orders for user {} in page {}", 
                orderPage.getContent().size(), userId, pageable.getPageNumber());
        
        return orderPage.map(this::toCustomerOrderResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrdersByStatus(Long userId, OrderStatus status) {
        log.info("Getting orders by status {} for user: {}", status, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        
        
        List<Order> orders = orderRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status);
        
        log.info("Found {} orders with status {} for user {}", orders.size(), status, userId);
        
        return orders.stream()
                .map(this::toCustomerOrderResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getMyOrdersCount(Long userId) {
        log.info("Counting orders for user:  {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        
        
        long count = orderRepository. countByUser(user);
        
        log.info("User {} has {} orders", userId, count);
        
        return count;
    }
    
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrderDetail(Long orderId, Long userId) {
        log.info("Getting order detail {} for user {}", orderId, userId);
        
        // Tái sử dụng method getOrderById đã có
        return getOrderById(orderId, userId);
    }
    
    
    
    // ========================================
    // ✅ THÊM CHỨC NĂNG HỦY ĐƠN HÀNG
    // ========================================
    
    @Override
    @Transactional
    public void cancelMyOrder(Long orderId, Long userId) {
        log.info("User {} attempting to cancel order {}", userId, orderId);
        
        // 1. Tìm đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log. error("Order not found: {}", orderId);
                    return new ResourceNotFoundException("Đơn hàng không tồn tại");
                });
        
        // 2. Kiểm tra quyền sở hữu
        if (! order.getUser().getId().equals(userId)) {
            log.warn("User {} tried to cancel order {} owned by user {}", 
                    userId, orderId, order.getUser().getId());
            throw new ForbiddenException("Bạn không có quyền hủy đơn hàng này");
        }
        
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Cannot cancel order {} with status {}", orderId, order.getStatus());
            throw new BadRequestException(
                "Chỉ có thể hủy đơn khi đơn đang chờ admin xác nhận. Sau khi admin xác nhận thì không thể hủy."
            );
        }
        
        // 4. Cập nhật trạng thái sang CANCELLED
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        
        // 5. Lưu đơn hàng
        orderRepository.save(order);
        
        // 6. Ghi log lịch sử trạng thái
        try {
            saveOrderStatusHistory(order, OrderStatus.CANCELLED, "Khách hàng tự hủy đơn");
        } catch (Exception e) {
            log.warn("Failed to save order status history for order {}: {}", orderId, e.getMessage());
        }
        
        if (Boolean.TRUE.equals(order.getStockDeducted())) {
            restoreProductStock(order);
            order.setStockDeducted(false);
            orderRepository.save(order);
            log.info("Product stock restored for cancelled order: {}", order.getOrderCode());
        }
        
        log.info("Order {} successfully cancelled by user {}. Status changed from {} to {}", 
                orderId, userId, oldStatus, OrderStatus.CANCELLED);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canCancelOrder(Long orderId, Long userId) {
        log.info("Checking if user {} can cancel order {}", userId, orderId);
        
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
            
            // Kiểm tra quyền sở hữu
            if (!order.getUser().getId().equals(userId)) {
                return false;
            }
            
            boolean canCancel = order.getStatus() == OrderStatus.PENDING;
            
            log.info("Order {} can be cancelled: {}", orderId, canCancel);
            return canCancel;
            
        } catch (Exception e) {
            log.error("Error checking cancel permission for order {}: {}", orderId, e.getMessage());
            return false;
        }
    }
    
    // ========================================
    // ✅ HELPER METHODS
    // ========================================
    
    private String getStatusDisplayName(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận"; 
            case SHIPPING -> "Đã giao";
            case DELIVERED -> "Giao thành công";
            case CANCELLED -> "Đã hủy";
        };
    }
    
    private void saveOrderStatusHistory(Order order, OrderStatus newStatus, String note) {
        try {
            OrderStatusHistory history = OrderStatusHistory.builder()
                    .order(order)
                    .status(newStatus)
                    .changedBy(note != null ? note : "USER")
                    .build();
            orderStatusHistoryRepository.save(history);
            log.info("Saved order status history for {}: status = {}, note = {}", order.getOrderCode(), newStatus, note);
        } catch (Exception e) {
            log.warn("Failed to save order status history for {}: {}", order.getOrderCode(), e.getMessage());
        }
    }
    
    private void restoreProductStock(Order order) {
        log.info("Restoring stock for cancelled order: {}", order.getOrderCode());
        
        List<OrderItem> orderItems = order.getItems();
        if (orderItems == null || orderItems.isEmpty()) {
            log.info("No order items found for order: {}", order.getOrderCode());
            return;
        }
        
        for (OrderItem item : orderItems) {
            Product product = item.getProduct();
            if (product != null && item.getQuantity() != null) {
                inventoryService.restore(product, item.getColor(), item.getSize(), item.getQuantity());
            }
        }
    }

    private OrderResponse toCustomerOrderResponse(Order order) {
        OrderResponse response = orderMapper.toOrderResponse(order);
        response.setCanCancel(order.getStatus() == OrderStatus.PENDING);
        OrderReturn orderReturn = orderReturnRepository.findByOrderId(order.getId()).orElse(null);
        boolean withinWindow = order.getCreatedAt() != null
                && !order.getCreatedAt().plusDays(3).isBefore(LocalDateTime.now());
        response.setCanReturn(order.getStatus() == OrderStatus.DELIVERED && withinWindow && orderReturn == null);
        response.setReturnStatus(orderReturn == null ? null : orderReturn.getStatus().name());
        return response;
    }

    
    
    
    
   
}