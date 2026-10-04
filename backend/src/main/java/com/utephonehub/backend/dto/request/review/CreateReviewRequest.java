package com.utephonehub.backend.dto.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateReviewRequest {

    @NotNull(message = "Vui lòng chọn đơn hàng đã nhận sản phẩm")
    private Long orderId;

    @NotNull(message = "Vui lòng chọn số sao đánh giá")
    @Min(value = 1, message = "Đánh giá tối thiểu 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa 5 sao")
    private Integer rating;

    @NotBlank(message = "Vui lòng nhập nội dung đánh giá")
    @Size(max = 2000, message = "Nội dung đánh giá không được vượt quá 2000 ký tự")
    private String comment;

    @NotNull(message = "Vui lòng đánh giá chất liệu sản phẩm")
    @Min(value = 1, message = "Đánh giá chất liệu tối thiểu 1 sao")
    @Max(value = 5, message = "Đánh giá chất liệu tối đa 5 sao")
    private Integer materialRating;

    @NotNull(message = "Vui lòng đánh giá kích cỡ và form dáng")
    @Min(value = 1, message = "Đánh giá form dáng tối thiểu 1 sao")
    @Max(value = 5, message = "Đánh giá form dáng tối đa 5 sao")
    private Integer fitRating;

    @NotNull(message = "Vui lòng đánh giá màu sắc sản phẩm")
    @Min(value = 1, message = "Đánh giá màu sắc tối thiểu 1 sao")
    @Max(value = 5, message = "Đánh giá màu sắc tối đa 5 sao")
    private Integer colorRating;

    @Size(max = 5, message = "Bạn chỉ có thể tải tối đa 5 ảnh")
    private List<@NotBlank @Size(max = 2048) String> imageUrls = new ArrayList<>();
}
