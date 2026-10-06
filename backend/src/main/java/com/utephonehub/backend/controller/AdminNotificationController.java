package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.response.notification.AdminNotificationResponse;
import com.utephonehub.backend.service.impl.AdminNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@Tag(name = "Admin Notifications", description = "Thông báo cho quản trị viên")
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @GetMapping
    @Operation(summary = "Danh sách thông báo admin, gồm yêu cầu hoàn tiền")
    public ResponseEntity<ApiResponse<AdminNotificationResponse.ListPayload>> list() {
        return ResponseEntity.ok(ApiResponse.success(
                "Danh sách thông báo",
                adminNotificationService.list()));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Đánh dấu một thông báo đã đọc")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id) {
        adminNotificationService.markRead(id);
        return ResponseEntity.ok(ApiResponse.success("Đã đọc thông báo", null));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả thông báo đã đọc")
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        adminNotificationService.markAllRead();
        return ResponseEntity.ok(ApiResponse.success("Đã đọc tất cả thông báo", null));
    }
}
