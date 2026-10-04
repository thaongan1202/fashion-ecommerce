package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.request.review.CreateReviewRequest;
import com.utephonehub.backend.dto.response.review.ProductReviewsResponse;

public interface IReviewService {
    ProductReviewsResponse getProductReviews(Long productId, Long userId);
    ProductReviewsResponse createProductReview(Long productId, Long userId, CreateReviewRequest request);
}
