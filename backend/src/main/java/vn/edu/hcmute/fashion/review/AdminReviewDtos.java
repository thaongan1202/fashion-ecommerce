package vn.edu.hcmute.fashion.review;

import java.time.OffsetDateTime;
import java.util.List;

public final class AdminReviewDtos {
    private AdminReviewDtos() {}
    public record StatusRequest(String status) {}
    public record ReviewItem(long id, long productId, String productName, long userId, String customerName,
                             String customerEmail, int rating, String comment, String imageUrl,
                             String status, OffsetDateTime createdAt) {}
    public record ReviewPage(List<ReviewItem> content, long totalElements, int page, int size, int totalPages) {}
}
