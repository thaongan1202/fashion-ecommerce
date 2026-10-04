package com.utephonehub.backend.dto.response.review;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductReviewsResponse {
    List<ReviewResponse> reviews;
    Double averageRating;
    Integer totalReviews;
    boolean canReview;
    List<ReviewOrderOption> eligibleOrders;
    List<Long> reviewedOrderIds;
    String eligibilityMessage;
}
