package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.response.order.OrderReturnResponse;
import com.utephonehub.backend.dto.response.order.ReturnStatisticsResponse;
import com.utephonehub.backend.entity.Order;
import com.utephonehub.backend.entity.OrderItem;
import com.utephonehub.backend.entity.OrderReturn;
import com.utephonehub.backend.entity.User;
import com.utephonehub.backend.enums.OrderStatus;
import com.utephonehub.backend.enums.ReturnStatus;
import com.utephonehub.backend.exception.BadRequestException;
import com.utephonehub.backend.exception.ForbiddenException;
import com.utephonehub.backend.exception.ResourceNotFoundException;
import com.utephonehub.backend.repository.OrderRepository;
import com.utephonehub.backend.repository.OrderReturnRepository;
import com.utephonehub.backend.repository.UserRepository;
import com.utephonehub.backend.service.IOrderReturnService;
import com.utephonehub.backend.service.InventoryService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderReturnServiceImpl implements IOrderReturnService {

    private static final int RETURN_WINDOW_DAYS = 3;

    private final OrderRepository orderRepository;
    private final OrderReturnRepository orderReturnRepository;
    private final UserRepository userRepository;
    private final ReturnEvidenceStorageService evidenceStorageService;
    private final InventoryService inventoryService;
    private final AdminNotificationService adminNotificationService;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public OrderReturnResponse createReturn(Long userId, Long orderId, String reason, MultipartFile evidence) {
        if (reason == null || reason.trim().length() < 5) {
            throw new BadRequestException("Vui lòng chọn hoặc nhập lý do hoàn hàng");
        }

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Bạn không có quyền hoàn đơn hàng này");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Chỉ được hoàn hàng sau khi đơn đã giao thành công");
        }
        if (order.getCreatedAt() == null || order.getCreatedAt().plusDays(RETURN_WINDOW_DAYS).isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Đã quá 3 ngày kể từ ngày đặt hàng nên không thể hoàn hàng");
        }
        if (orderReturnRepository.findByOrderId(orderId).isPresent()) {
            throw new BadRequestException("Đơn hàng này đã có yêu cầu hoàn");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        String evidenceUrl = evidenceStorageService.store(userId, evidence);

        OrderReturn orderReturn = OrderReturn.builder()
                .order(order)
                .user(user)
                .reason(reason.trim())
                .evidenceUrl(evidenceUrl)
                .status(ReturnStatus.PENDING)
                .refundAmount(BigDecimal.ZERO)
                .build();
        orderReturn = orderReturnRepository.save(orderReturn);
        adminNotificationService.notifyReturnRequested(
                orderReturn.getId(),
                order.getOrderCode(),
                user.getFullName(),
                user.getEmail(),
                order.getTotalAmount(),
                orderReturn.getReason());
        log.info("User {} requested return for order {}", userId, order.getOrderCode());
        return OrderReturnResponse.fromEntity(orderReturn);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderReturnResponse getMyReturn(Long userId, Long orderId) {
        OrderReturn orderReturn = orderReturnRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng chưa có yêu cầu hoàn"));
        if (!orderReturn.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Bạn không có quyền xem yêu cầu hoàn này");
        }
        return OrderReturnResponse.fromEntity(orderReturn);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderReturnResponse> getReturns(ReturnStatus status, Pageable pageable) {
        Page<OrderReturn> page = status == null
                ? orderReturnRepository.findAllByOrderByCreatedAtDesc(pageable)
                : orderReturnRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return page.map(OrderReturnResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnStatisticsResponse getStatistics() {
        BigDecimal gross = orderRepository.calculateTotalRevenueByStatus(OrderStatus.DELIVERED);
        if (gross == null) {
            gross = BigDecimal.ZERO;
        }
        BigDecimal refunded = orderReturnRepository.sumApprovedRefundAmount();
        if (refunded == null) {
            refunded = BigDecimal.ZERO;
        }
        BigDecimal net = gross.subtract(refunded);
        if (net.compareTo(BigDecimal.ZERO) < 0) {
            net = BigDecimal.ZERO;
        }
        long approved = orderReturnRepository.countByStatus(ReturnStatus.APPROVED);
        return ReturnStatisticsResponse.builder()
                .pendingCount(orderReturnRepository.countByStatus(ReturnStatus.PENDING))
                .approvedCount(approved)
                .rejectedCount(orderReturnRepository.countByStatus(ReturnStatus.REJECTED))
                .refundedOrderCount(approved)
                .totalRefundAmount(refunded)
                .grossRevenue(gross)
                .revenueAfterRefund(net)
                .build();
    }

    @Override
    @Transactional
    public OrderReturnResponse approve(Long returnId) {
        OrderReturn orderReturn = loadPending(returnId);
        Order order = orderReturn.getOrder();
        User user = orderReturn.getUser();
        BigDecimal amount = refundAmountOf(order);
        Long walletUserId = user.getId();

        try {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getProduct() != null && item.getQuantity() != null && item.getQuantity() > 0) {
                        inventoryService.restore(item.getProduct(), item.getColor(), item.getSize(), item.getQuantity());
                    }
                }
            }
        } catch (Exception exception) {
            log.error("Không hoàn được tồn kho cho yêu cầu {}, vẫn hoàn tiền vào ví", returnId, exception);
        }

        orderReturn.setStatus(ReturnStatus.APPROVED);
        orderReturn.setRefundAmount(amount);
        orderReturn.setReviewedAt(LocalDateTime.now());
        orderReturnRepository.saveAndFlush(orderReturn);
        OrderReturnResponse response = OrderReturnResponse.fromEntity(orderReturn);

        if (entityManager.contains(user)) {
            entityManager.detach(user);
        }
        int credited = userRepository.creditWallet(walletUserId, amount);
        if (credited != 1) {
            throw new BadRequestException("Không thể cộng tiền hoàn vào ví khách");
        }
        log.info("Approved return {} and refunded {} to user {}", returnId, amount, walletUserId);
        return response;
    }

    @Override
    @Transactional
    public OrderReturnResponse reject(Long returnId, String adminNote) {
        if (adminNote == null || adminNote.isBlank()) {
            throw new BadRequestException("Lý do từ chối không được để trống");
        }
        OrderReturn orderReturn = loadPending(returnId);
        orderReturn.setStatus(ReturnStatus.REJECTED);
        orderReturn.setAdminNote(adminNote.trim());
        orderReturn.setRefundAmount(BigDecimal.ZERO);
        orderReturn.setReviewedAt(LocalDateTime.now());
        orderReturnRepository.save(orderReturn);
        return OrderReturnResponse.fromEntity(orderReturn);
    }

    private OrderReturn loadPending(Long returnId) {
        OrderReturn orderReturn = orderReturnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu hoàn hàng không tồn tại"));
        if (orderReturn.getStatus() != ReturnStatus.PENDING) {
            throw new BadRequestException("Yêu cầu hoàn này đã được xử lý");
        }
        return orderReturn;
    }

    private BigDecimal refundAmountOf(Order order) {
        BigDecimal amount = order.getTotalAmount();
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            return amount;
        }
        if (order.getItems() == null) {
            throw new BadRequestException("Đơn hàng không có số tiền để hoàn vào ví");
        }
        BigDecimal summed = order.getItems().stream()
                .filter(item -> item.getPrice() != null && item.getQuantity() != null)
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (summed.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Đơn hàng không có số tiền để hoàn vào ví");
        }
        return summed;
    }
}
