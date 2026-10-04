package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.request.review.CreateReviewRequest;
import com.utephonehub.backend.dto.response.review.ProductReviewsResponse;
import com.utephonehub.backend.dto.response.review.AdminReviewResponse;
import com.utephonehub.backend.dto.response.review.ReviewedProductOption;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IReviewService {
    void deleteAdminReview(Long reviewId);
    Page<AdminReviewResponse> getAdminReviews(int page, int size, Long productId, Integer rating);
    List<ReviewedProductOption> getReviewedProducts();
    ProductReviewsResponse getProductReviews(Long productId, Long userId);
    ProductReviewsResponse createProductReview(Long productId, Long userId, CreateReviewRequest request);
    List<String> uploadReviewImages(Long productId, Long orderId, Long userId, List<MultipartFile> files);
}
