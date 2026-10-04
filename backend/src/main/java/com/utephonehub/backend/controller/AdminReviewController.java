package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.response.review.AdminReviewResponse;
import com.utephonehub.backend.dto.response.review.ReviewedProductOption;
import com.utephonehub.backend.service.IReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final IReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminReviewResponse>>> getReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer rating) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách đánh giá thành công",
                reviewService.getAdminReviews(page, size, productId, rating)));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ReviewedProductOption>>> getReviewedProducts() {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách sản phẩm đã được đánh giá thành công",
                reviewService.getReviewedProducts()));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteAdminReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa đánh giá", null));
    }
}
