package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ReturnEvidenceStorageService {

    private static final long MAX_FILE_SIZE = 25L * 1024L * 1024L;
    private static final Set<String> ALLOWED = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");

    @Value("${app.return.upload-dir:uploads/returns}")
    private String uploadDirectory;

    public String store(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Hình ảnh minh chứng là bắt buộc");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Minh chứng không được vượt quá 25 MB");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED.contains(contentType)) {
            throw new BadRequestException("Chỉ chấp nhận ảnh JPG, PNG, WEBP hoặc GIF");
        }

        String extension = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "bin";
        };

        Path root = Path.of(uploadDirectory).toAbsolutePath().normalize();
        Path userDirectory = root.resolve(String.valueOf(userId)).normalize();
        if (!userDirectory.startsWith(root)) {
            throw new BadRequestException("Đường dẫn lưu minh chứng không hợp lệ");
        }

        String fileName = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(userDirectory);
            Files.write(userDirectory.resolve(fileName), file.getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException exception) {
            log.error("Unable to store return evidence for user {}", userId, exception);
            throw new BadRequestException("Không thể lưu minh chứng, vui lòng thử lại");
        }
        return "/uploads/returns/" + userId + "/" + fileName;
    }
}
