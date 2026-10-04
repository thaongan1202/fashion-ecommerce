package com.utephonehub.backend.dto.response.review;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class AdminReviewResponse {
    Long id;
    Long productId;
    String productName;
    String productThumbnailUrl;
    String authorName;
    Integer rating;
    String comment;
    LocalDateTime createdAt;
    boolean verifiedPurchase;
    Integer materialRating;
    Integer fitRating;
    Integer colorRating;
    String productColor;
    String productSize;
    List<String> imageUrls;
}
