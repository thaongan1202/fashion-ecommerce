package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.request.review.CreateReviewRequest;
import com.utephonehub.backend.dto.response.review.ProductReviewsResponse;
import com.utephonehub.backend.dto.response.review.ReviewOrderOption;
import com.utephonehub.backend.dto.response.review.ReviewResponse;
import com.utephonehub.backend.entity.Order;
import com.utephonehub.backend.entity.Product;
import com.utephonehub.backend.entity.Review;
import com.utephonehub.backend.entity.User;
import com.utephonehub.backend.enums.OrderStatus;
import com.utephonehub.backend.exception.BadRequestException;
import com.utephonehub.backend.exception.ConflictException;
import com.utephonehub.backend.exception.ResourceNotFoundException;
import com.utephonehub.backend.exception.UnauthorizedException;
import com.utephonehub.backend.repository.OrderRepository;
import com.utephonehub.backend.repository.ProductRepository;
import com.utephonehub.backend.repository.ReviewRepository;
import com.utephonehub.backend.repository.UserRepository;
import com.utephonehub.backend.service.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductReviewsResponse getProductReviews(Long productId, Long userId) {
        getProduct(productId);

        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(this::toReviewResponse)
                .collect(Collectors.toList());
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        List<ReviewOrderOption> eligibleOrders = userId == null
                ? List.of()
                : getEligibleOrders(productId, userId);

        String eligibilityMessage;
        if (userId == null) {
            eligibilityMessage = "Đăng nhập để kiểm tra quyền đánh giá sản phẩm này.";
        } else if (eligibleOrders.isEmpty()) {
            eligibilityMessage = "Bạn chỉ có thể đánh giá sản phẩm trong đơn hàng đã giao thành công và chưa được đánh giá.";
        } else {
            eligibilityMessage = "Chọn đơn hàng đã nhận sản phẩm để gửi đánh giá.";
        }

        return ProductReviewsResponse.builder()
                .reviews(reviewResponses)
                .averageRating(averageRating)
                .totalReviews(reviewResponses.size())
                .canReview(!eligibleOrders.isEmpty())
                .eligibleOrders(eligibleOrders)
                .eligibilityMessage(eligibilityMessage)
                .build();
    }

    @Override
    @Transactional
    public ProductReviewsResponse createProductReview(Long productId, Long userId, CreateReviewRequest request) {
        if (userId == null) {
            throw new UnauthorizedException("Vui lòng đăng nhập để đánh giá sản phẩm");
        }

        Product product = getProduct(productId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Tài khoản không tồn tại hoặc phiên đăng nhập đã hết hạn"));

        boolean purchasedAndDelivered = reviewRepository.existsDeliveredPurchase(
                request.getOrderId(), userId, productId, OrderStatus.DELIVERED);
        if (!purchasedAndDelivered) {
            throw new BadRequestException("Đơn hàng phải thuộc về bạn, có sản phẩm này và đã giao thành công mới được đánh giá");
        }

        if (reviewRepository.existsByUserIdAndProductIdAndOrderId(userId, productId, request.getOrderId())) {
            throw new ConflictException("Bạn đã đánh giá sản phẩm này trong đơn hàng đã chọn");
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .order(orderRepository.getReferenceById(request.getOrderId()))
                .rating(request.getRating())
                .comment(request.getComment().trim())
                .build();

        try {
            reviewRepository.saveAndFlush(review);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Bạn đã đánh giá sản phẩm này trong đơn hàng đã chọn", exception);
        }

        return getProductReviews(productId, userId);
    }

    private List<ReviewOrderOption> getEligibleOrders(Long productId, Long userId) {
        return reviewRepository.findDeliveredOrdersWithProduct(userId, productId, OrderStatus.DELIVERED).stream()
                .filter(order -> !reviewRepository.existsByUserIdAndProductIdAndOrderId(userId, productId, order.getId()))
                .map(order -> ReviewOrderOption.builder()
                        .orderId(order.getId())
                        .orderCode(order.getOrderCode())
                        .build())
                .collect(Collectors.toList());
    }

    private Product getProduct(Long productId) {
        return productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
    }

    private ReviewResponse toReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .authorName(review.getUser().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .verifiedPurchase(review.getOrder() != null)
                .build();
    }
}
