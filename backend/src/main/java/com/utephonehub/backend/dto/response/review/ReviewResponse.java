package com.utephonehub.backend.dto.response.review;

import java.time.LocalDateTime;
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
}
