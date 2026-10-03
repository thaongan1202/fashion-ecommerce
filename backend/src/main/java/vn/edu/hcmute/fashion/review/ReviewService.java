package vn.edu.hcmute.fashion.review;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReviewService {
    private final ReviewRepository repository;
    private final ReviewImageStorageService imageStorage;
    public ReviewService(ReviewRepository repository, ReviewImageStorageService imageStorage) {
        this.repository = repository;
        this.imageStorage = imageStorage;
    }

    public ReviewPage list(long productId, int page, int size) {
        requireProduct(productId);
        validatePage(page, size);
        long total = repository.countApproved(productId);
        return new ReviewPage(repository.findApproved(productId, size, Math.toIntExact((long) page * size)), total, size, page,
                total == 0 ? 0 : (int) Math.ceil((double) total / size));
    }

    public ReviewResponse create(long productId, long userId, int rating, String comment, Long variantId,
            Integer heightCm, BigDecimal weightKg, String imageUrl) {
        requireProduct(productId);
        validateReview(rating, comment, variantId, heightCm, weightKg, imageUrl);
        if (!repository.hasDeliveredVariant(userId, productId, variantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn chỉ có thể đánh giá sản phẩm trong đơn hàng đã giao thành công");
        }
        if (repository.alreadyReviewed(userId, productId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bạn đã đánh giá sản phẩm này");
        }
        try {
            return repository.create(userId, productId, rating, comment, "PENDING",
                    variantId, heightCm, weightKg, imageUrl);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bạn đã đánh giá sản phẩm này");
        }
    }

    public MineReview mine(long productId, long userId) {
        requireProduct(productId);
        List<PurchasedVariant> variants = repository.findDeliveredVariants(userId, productId);
        return new MineReview(!variants.isEmpty(), repository.findMine(userId, productId), variants);
    }

    public ReviewResponse update(long productId, long reviewId, long userId, int rating, String comment,
            Long variantId, Integer heightCm, BigDecimal weightKg, String imageUrl) {
        requireProduct(productId);
        validateReview(rating, comment, variantId, heightCm, weightKg, imageUrl);
        if (!repository.hasDeliveredVariant(userId, productId, variantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn chỉ có thể sửa đánh giá khi đã mua và nhận sản phẩm");
        }
        ReviewResponse review = repository.update(reviewId, userId, productId, rating, comment,
                "PENDING", variantId, heightCm, weightKg, imageUrl);
        if (review == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá của bạn");
        return review;
    }

    public String uploadImage(long productId, long userId, MultipartFile file) {
        requireProduct(productId);
        if (repository.findDeliveredVariants(userId, productId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only customers with a delivered purchase can upload a review image");
        }
        return imageStorage.save(file);
    }

    public List<AdminReviewResponse> listPending(long userId) {
        requireAdmin(userId);
        return repository.findPending();
    }

    public AdminReviewResponse changeStatus(long userId, long reviewId, String status) {
        requireAdmin(userId);
        if (reviewId <= 0 || !("APPROVED".equals(status) || "HIDDEN".equals(status))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái review không hợp lệ");
        }
        AdminReviewResponse result = repository.changeStatus(reviewId, status);
        if (result == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy review đang chờ duyệt");
        return result;
    }

    private void requireAdmin(long userId) {
        if (!repository.isActiveAdmin(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ Admin mới được quản lý review");
    }

    private void validateReview(int rating, String comment, Long variantId, Integer heightCm, BigDecimal weightKg, String imageUrl) {
        if (rating < 1 || rating > 5) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rating phải từ 1 đến 5");
        if (comment == null || comment.isBlank() || comment.length() > 5000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "comment là bắt buộc và tối đa 5000 ký tự");
        if (variantId == null || variantId <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cần chọn phân loại đã mua");
        if (heightCm != null && (heightCm < 100 || heightCm > 250)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chiều cao phải từ 100 đến 250 cm");
        if (weightKg != null && (weightKg.compareTo(new BigDecimal("20")) < 0 || weightKg.compareTo(new BigDecimal("300")) > 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cân nặng phải từ 20 đến 300 kg");
        }
        if (imageUrl != null && !imageStorage.isStoredImage(imageUrl)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid review image URL");
        }
    }

    private void requireProduct(long productId) {
        if (!repository.productExists(productId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm");
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page phải từ 0; size từ 1 đến 100");
    }

    public record ReviewPage(List<ReviewResponse> content, long totalElements, int size, int number, int totalPages) {}
    public record MineReview(boolean eligible, ReviewResponse review, List<PurchasedVariant> purchasedVariants) {}
}
