package vn.edu.hcmute.fashion.review;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReviewResponse(long id, String userName, int rating, String comment, String imageUrl, String status,
        Long variantId, String size, String color, Integer heightCm, BigDecimal weightKg, OffsetDateTime createdAt) {}
