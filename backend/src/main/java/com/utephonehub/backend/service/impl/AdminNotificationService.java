package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.response.notification.AdminNotificationResponse;
import com.utephonehub.backend.entity.AdminNotification;
import com.utephonehub.backend.entity.User;
import com.utephonehub.backend.enums.UserRole;
import com.utephonehub.backend.exception.ResourceNotFoundException;
import com.utephonehub.backend.repository.AdminNotificationRepository;
import com.utephonehub.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationService {

    private final AdminNotificationRepository adminNotificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public void notifyReturnRequested(Long returnId, String orderCode, String customerName,
                                      String customerEmail, BigDecimal amount, String reason) {
        String money = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(
                amount == null ? BigDecimal.ZERO : amount);
        String message = String.format(
                "%s (%s) yêu cầu hoàn đơn %s. Số tiền hoàn: %s₫. Lý do: %s",
                customerName, customerEmail, orderCode, money, reason);

        adminNotificationRepository.save(AdminNotification.builder()
                .title("Yêu cầu hoàn tiền")
                .message(message)
                .type("RETURN_REQUEST")
                .referenceId(returnId)
                .read(false)
                .build());

        emailAdmins("Yêu cầu hoàn tiền đơn " + orderCode, message);
    }

    @Transactional(readOnly = true)
    public AdminNotificationResponse.ListPayload list() {
        List<AdminNotificationResponse> items = adminNotificationRepository.findTop30ByOrderByCreatedAtDesc()
                .stream()
                .map(AdminNotificationResponse::fromEntity)
                .toList();
        return AdminNotificationResponse.ListPayload.builder()
                .unreadCount(adminNotificationRepository.countByReadFalse())
                .items(items)
                .build();
    }

    @Transactional
    public void markRead(Long id) {
        AdminNotification notification = adminNotificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo không tồn tại"));
        notification.setRead(true);
        adminNotificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead() {
        adminNotificationRepository.markAllRead();
    }

    private void emailAdmins(String subject, String body) {
        try {
            List<User> admins = userRepository.findByRoleAndDeletedAtIsNull(UserRole.ADMIN);
            for (User admin : admins) {
                if (admin.getEmail() == null || admin.getEmail().isBlank()) {
                    continue;
                }
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setTo(admin.getEmail());
                mail.setSubject(subject);
                mail.setText(body);
                mailSender.send(mail);
            }
        } catch (Exception exception) {
            log.warn("Không gửi được email thông báo hoàn tiền cho admin: {}", exception.getMessage());
        }
    }
}
