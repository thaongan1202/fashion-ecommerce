package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.review.CreateReviewRequest;
import com.utephonehub.backend.dto.response.review.ProductReviewsResponse;
import com.utephonehub.backend.service.IReviewService;
import com.utephonehub.backend.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
@RequiredArgsConstructor
public class ProductReviewController {

    private final IReviewService reviewService;
    private final SecurityUtils securityUtils;

    @PostMapping(path = "/images", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<List<String>>> uploadReviewImages(
            @PathVariable Long productId,
            @RequestParam("orderId") Long orderId,
            @RequestParam("files") List<MultipartFile> files,
            HttpServletRequest request) {
        Long userId = securityUtils.getCurrentUserId(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Tải ảnh đánh giá thành công",
                reviewService.uploadReviewImages(productId, orderId, userId, files)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProductReviewsResponse>> getProductReviews(
            @PathVariable Long productId,
            HttpServletRequest request) {
        Long userId = securityUtils.getUserIdIfAuthenticated(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách đánh giá thành công",
                reviewService.getProductReviews(productId, userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductReviewsResponse>> createProductReview(
            @PathVariable Long productId,
            @Valid @RequestBody CreateReviewRequest reviewRequest,
            HttpServletRequest request) {
        Long userId = securityUtils.getCurrentUserId(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
                "Gửi đánh giá thành công",
                reviewService.createProductReview(productId, userId, reviewRequest)));
    }
}
