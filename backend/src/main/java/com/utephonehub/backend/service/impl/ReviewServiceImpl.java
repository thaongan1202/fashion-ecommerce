package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.request.review.CreateReviewRequest;
import com.utephonehub.backend.dto.response.review.AdminReviewResponse;
import com.utephonehub.backend.dto.response.review.ProductReviewsResponse;
import com.utephonehub.backend.dto.response.review.ReviewedProductOption;
import com.utephonehub.backend.dto.response.review.ReviewOrderOption;
import com.utephonehub.backend.dto.response.review.ReviewResponse;
import com.utephonehub.backend.entity.Order;
import com.utephonehub.backend.entity.OrderItem;
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
import com.utephonehub.backend.repository.ReviewedProductProjection;
import com.utephonehub.backend.repository.UserRepository;
import com.utephonehub.backend.service.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewImageStorageService reviewImageStorageService;

    @Override
    @Transactional
    public void deleteAdminReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá"));
        List<String> imageUrls = review.getImageUrls() == null ? List.of() : List.copyOf(review.getImageUrls());
        reviewRepository.delete(review);
        reviewRepository.flush();
        reviewImageStorageService.deleteImages(imageUrls);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReviewResponse> getAdminReviews(int page, int size, Long productId, Integer rating) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new BadRequestException("Số sao phải nằm trong khoảng từ 1 đến 5");
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));
        return reviewRepository.findAdminReviews(
                        productId,
                        rating,
                        PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")
                                .and(Sort.by(Sort.Direction.DESC, "id"))))
                .map(this::toAdminReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewedProductOption> getReviewedProducts() {
        return reviewRepository.findReviewedProducts().stream()
                .map(this::toReviewedProductOption)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductReviewsResponse getProductReviews(Long productId, Long userId) {
        getProduct(productId);

        List<Review> reviews = reviewRepository.findTop30ByProductIdOrderByCreatedAtDesc(productId);
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(this::toReviewResponse)
                .collect(Collectors.toList());
        Double averageRatingResult = reviewRepository.calculateAverageRatingByProductId(productId);
        double averageRating = averageRatingResult == null ? 0.0 : averageRatingResult;
        int totalReviews = reviewRepository.countReviewsByProductId(productId).intValue();

        List<Order> deliveredOrders = userId == null
                ? List.of()
                : reviewRepository.findDeliveredOrdersWithProduct(userId, productId, OrderStatus.DELIVERED);
        List<Long> deliveredOrderIds = deliveredOrders.stream().map(Order::getId).toList();
        List<Long> reviewedOrderIds = deliveredOrderIds.isEmpty()
                ? List.of()
                : reviewRepository.findReviewedOrderIds(userId, productId, deliveredOrderIds);
        Set<Long> reviewedOrderIdSet = Set.copyOf(reviewedOrderIds);
        List<ReviewOrderOption> eligibleOrders = deliveredOrders.stream()
                .filter(order -> !reviewedOrderIdSet.contains(order.getId()))
                .map(order -> toReviewOrderOption(order, productId))
                .toList();

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
                .totalReviews(totalReviews)
                .canReview(!eligibleOrders.isEmpty())
                .eligibleOrders(eligibleOrders)
                .reviewedOrderIds(reviewedOrderIds)
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

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy đơn hàng đã giao"));
        OrderItem purchasedItem = order.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Đơn hàng không có sản phẩm này"));

        List<String> imageUrls = request.getImageUrls() == null ? List.of() : request.getImageUrls();
        String ownedUploadPrefix = "/uploads/reviews/" + userId + "/";
        if (imageUrls.stream().anyMatch(url -> url == null || !url.startsWith(ownedUploadPrefix)
                || url.contains("..") || url.contains("\\"))) {
            throw new BadRequestException("Ảnh đánh giá không hợp lệ hoặc không thuộc tài khoản của bạn");
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .order(order)
                .rating(request.getRating())
                .comment(request.getComment().trim())
                .materialRating(request.getMaterialRating())
                .fitRating(request.getFitRating())
                .colorRating(request.getColorRating())
                .productColor(purchasedItem.getColor())
                .productSize(purchasedItem.getSize())
                .imageUrls(imageUrls)
                .build();

        try {
            reviewRepository.saveAndFlush(review);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Bạn đã đánh giá sản phẩm này trong đơn hàng đã chọn", exception);
        }

        return getProductReviews(productId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> uploadReviewImages(Long productId, Long orderId, Long userId, List<MultipartFile> files) {
        if (userId == null) {
            throw new UnauthorizedException("Vui lòng đăng nhập để tải ảnh đánh giá");
        }
        getProduct(productId);
        if (!reviewRepository.existsDeliveredPurchase(orderId, userId, productId, OrderStatus.DELIVERED)) {
            throw new BadRequestException("Chỉ có thể tải ảnh cho sản phẩm trong đơn hàng đã giao thành công");
        }
        return reviewImageStorageService.store(userId, files);
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
                .materialRating(review.getMaterialRating())
                .fitRating(review.getFitRating())
                .colorRating(review.getColorRating())
                .productColor(review.getProductColor())
                .productSize(review.getProductSize())
                .imageUrls(review.getImageUrls() == null ? List.of() : List.copyOf(review.getImageUrls()))
                .build();
    }

    private AdminReviewResponse toAdminReviewResponse(Review review) {
        Product product = review.getProduct();
        return AdminReviewResponse.builder()
                .id(review.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productThumbnailUrl(product.getThumbnailUrl())
                .authorName(review.getUser().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .verifiedPurchase(review.getOrder() != null)
                .materialRating(review.getMaterialRating())
                .fitRating(review.getFitRating())
                .colorRating(review.getColorRating())
                .productColor(review.getProductColor())
                .productSize(review.getProductSize())
                .imageUrls(review.getImageUrls() == null ? List.of() : List.copyOf(review.getImageUrls()))
                .build();
    }

    private ReviewedProductOption toReviewedProductOption(ReviewedProductProjection product) {
        return ReviewedProductOption.builder()
                .id(product.getId())
                .name(product.getName())
                .reviewCount(product.getReviewCount())
                .build();
    }

    private ReviewOrderOption toReviewOrderOption(Order order, Long productId) {
        OrderItem item = order.getItems().stream()
                .filter(orderItem -> orderItem.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
        return ReviewOrderOption.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .productColor(item == null ? null : item.getColor())
                .productSize(item == null ? null : item.getSize())
                .build();
    }
}
