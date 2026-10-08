package com.utephonehub.backend.dto.response.notification;

import com.utephonehub.backend.entity.AdminNotification;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminNotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String type;
    private Long referenceId;
    private boolean read;
    private LocalDateTime createdAt;

    public static AdminNotificationResponse fromEntity(AdminNotification entity) {
        return AdminNotificationResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .type(entity.getType())
                .referenceId(entity.getReferenceId())
                .read(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Data
    @Builder
    public static class ListPayload {
        private long unreadCount;
        private List<AdminNotificationResponse> items;
    }
}
