package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.order.RejectReturnRequest;
import com.utephonehub.backend.dto.response.order.OrderReturnResponse;
import com.utephonehub.backend.dto.response.order.ReturnStatisticsResponse;
import com.utephonehub.backend.enums.ReturnStatus;
import com.utephonehub.backend.service.IOrderReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/returns")
@RequiredArgsConstructor
@Tag(name = "Admin Returns", description = "Quản lý hoàn hàng")
public class AdminReturnController {

    private final IOrderReturnService orderReturnService;

    @GetMapping
    @Operation(summary = "Danh sách yêu cầu hoàn hàng")
    public ResponseEntity<ApiResponse<Page<OrderReturnResponse>>> list(
            @RequestParam(required = false) ReturnStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<OrderReturnResponse> result = orderReturnService.getReturns(status, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hoàn hàng thành công", result));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Thống kê hoàn hàng và doanh thu sau hoàn")
    public ResponseEntity<ApiResponse<ReturnStatisticsResponse>> statistics() {
        return ResponseEntity.ok(ApiResponse.success("Thống kê hoàn hàng", orderReturnService.getStatistics()));
    }

    @PostMapping("/{returnId}/approve")
    @Operation(summary = "Xác nhận hoàn hàng, hoàn tiền vào ví và trừ doanh thu")
    public ResponseEntity<ApiResponse<OrderReturnResponse>> approve(@PathVariable Long returnId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã xác nhận hoàn hàng và hoàn tiền vào ví khách",
                orderReturnService.approve(returnId)));
    }

    @PostMapping("/{returnId}/reject")
    @Operation(summary = "Từ chối hoàn hàng")
    public ResponseEntity<ApiResponse<OrderReturnResponse>> reject(
            @PathVariable Long returnId,
            @Valid @RequestBody RejectReturnRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Đã từ chối yêu cầu hoàn hàng",
                orderReturnService.reject(returnId, request.getAdminNote())));
    }
}
