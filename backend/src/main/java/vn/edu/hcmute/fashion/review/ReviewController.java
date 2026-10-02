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
import java.security.Principal;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
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
    public ReviewService.MineReview mine(@PathVariable long productId, Principal principal) {
        return service.mine(productId, requireUserId(principal));
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReviewImageResponse uploadImage(@PathVariable long productId, Principal principal,
            @RequestPart("file") MultipartFile file) {
        return new ReviewImageResponse(service.uploadImage(productId, requireUserId(principal), file));
    }

    @PostMapping
    public ReviewResponse create(@PathVariable long productId, Principal principal,
            @Valid @RequestBody CreateReviewRequest request) {
        return service.create(productId, requireUserId(principal), request.rating(), request.comment(), request.variantId(), request.heightCm(), request.weightKg(), request.imageUrl());
    }

    @PutMapping("/{reviewId}")
    public ReviewResponse update(@PathVariable long productId, @PathVariable long reviewId, Principal principal,
            @Valid @RequestBody CreateReviewRequest request) {
        return service.update(productId, reviewId, requireUserId(principal), request.rating(), request.comment(), request.variantId(), request.heightCm(), request.weightKg(), request.imageUrl());
    }

    private long requireUserId(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Đăng nhập trước khi đánh giá sản phẩm");
        }
        try {
            long userId = Long.parseLong(principal.getName());
            if (userId > 0) return userId;
        } catch (NumberFormatException ignored) {
            // Demo and future JWT principals use the user ID as their subject.
        }
        throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ");
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
