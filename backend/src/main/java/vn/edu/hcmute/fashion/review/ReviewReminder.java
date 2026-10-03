package vn.edu.hcmute.fashion.review;

import java.time.OffsetDateTime;

public record ReviewReminder(long productId, String productName, OffsetDateTime deliveredAt) {}
