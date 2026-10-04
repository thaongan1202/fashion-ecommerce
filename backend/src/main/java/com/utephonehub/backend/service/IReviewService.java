package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.request.review.CreateReviewRequest;
import com.utephonehub.backend.dto.response.review.ProductReviewsResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IReviewService {
    ProductReviewsResponse getProductReviews(Long productId, Long userId);
    ProductReviewsResponse createProductReview(Long productId, Long userId, CreateReviewRequest request);
    List<String> uploadReviewImages(Long productId, Long orderId, Long userId, List<MultipartFile> files);
}
