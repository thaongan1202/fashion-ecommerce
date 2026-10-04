package com.utephonehub.backend.dto.response.review;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReviewResponse {
    Long id;
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
