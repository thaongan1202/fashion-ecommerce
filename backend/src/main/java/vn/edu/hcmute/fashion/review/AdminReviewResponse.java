package vn.edu.hcmute.fashion.review;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AdminReviewResponse(long id, long productId, String productName, String userName,
        int rating, String comment, String imageUrl, String status, Long variantId,
        String size, String color, Integer heightCm, BigDecimal weightKg, OffsetDateTime createdAt) {}
