package vn.edu.hcmute.fashion.review;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcmute.fashion.shared.DemoAuthController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ReviewController {
    private final ReviewService service;
    public ReviewController(ReviewService service) { this.service = service; }

    @GetMapping
    public ReviewService.ReviewPage list(@PathVariable long productId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {
        return service.list(productId, page, size);
    }

    @GetMapping("/mine")
    public ReviewService.MineReview mine(@PathVariable long productId, HttpSession session) {
        return service.mine(productId, requireUserId(session));
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReviewImageResponse uploadImage(@PathVariable long productId, HttpSession session,
            @RequestPart("file") MultipartFile file) {
        return new ReviewImageResponse(service.uploadImage(productId, requireUserId(session), file));
    }

    @PostMapping
    public ReviewResponse create(@PathVariable long productId, HttpSession session,
            @Valid @RequestBody CreateReviewRequest request) {
        return service.create(productId, requireUserId(session), request.rating(), request.comment(), request.variantId(), request.heightCm(), request.weightKg(), request.imageUrl());
    }

    @PutMapping("/{reviewId}")
    public ReviewResponse update(@PathVariable long productId, @PathVariable long reviewId, HttpSession session,
            @Valid @RequestBody CreateReviewRequest request) {
        return service.update(productId, reviewId, requireUserId(session), request.rating(), request.comment(), request.variantId(), request.heightCm(), request.weightKg(), request.imageUrl());
    }

    private long requireUserId(HttpSession session) {
        Object userId = session.getAttribute(DemoAuthController.SESSION_USER_ID);
        if (userId instanceof Long id) return id;
        throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Đăng nhập trước khi đánh giá sản phẩm");
    }

    public record CreateReviewRequest(
            @Min(1) @Max(5) int rating,
            @NotBlank @Size(max = 5000) String comment,
            @NotNull Long variantId,
            @Min(100) @Max(250) Integer heightCm,
            @DecimalMin("20.0") @DecimalMax("300.0") BigDecimal weightKg,
            @Size(max = 1000) String imageUrl) {}

    public record ReviewImageResponse(String imageUrl) {}
}
