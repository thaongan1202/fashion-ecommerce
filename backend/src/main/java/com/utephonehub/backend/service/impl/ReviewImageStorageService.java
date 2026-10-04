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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ReviewImageStorageService {

    private static final int MAX_FILES = 5;
    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;

    @Value("${app.review.upload-dir:uploads/reviews}")
    private String uploadDirectory;

    public List<String> store(Long userId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        if (files.size() > MAX_FILES) {
            throw new BadRequestException("Bạn chỉ có thể tải tối đa 5 ảnh");
        }

        List<PendingImage> pendingImages = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new BadRequestException("Ảnh tải lên không được để trống");
            }
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new BadRequestException("Mỗi ảnh không được vượt quá 5 MB");
            }

            try {
                byte[] bytes = file.getBytes();
                String extension = imageExtension(bytes);
                pendingImages.add(new PendingImage(UUID.randomUUID() + "." + extension, bytes));
            } catch (IOException exception) {
                throw new BadRequestException("Không thể đọc ảnh tải lên", exception);
            }
        }

        Path root = Path.of(uploadDirectory).toAbsolutePath().normalize();
        Path userDirectory = root.resolve(String.valueOf(userId)).normalize();
        if (!userDirectory.startsWith(root)) {
            throw new BadRequestException("Đường dẫn lưu ảnh không hợp lệ");
        }

        try {
            Files.createDirectories(userDirectory);
            List<String> urls = new ArrayList<>(pendingImages.size());
            for (PendingImage image : pendingImages) {
                Files.write(userDirectory.resolve(image.fileName()), image.bytes(), StandardOpenOption.CREATE_NEW);
                urls.add("/uploads/reviews/" + userId + "/" + image.fileName());
            }
            return urls;
        } catch (IOException exception) {
            log.error("Unable to save review images for user {}", userId, exception);
            throw new BadRequestException("Không thể lưu ảnh đánh giá, vui lòng thử lại");
        }
    }

    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;

        Path root = Path.of(uploadDirectory).toAbsolutePath().normalize();
        String prefix = "/uploads/reviews/";
        for (String imageUrl : imageUrls) {
            if (imageUrl == null || !imageUrl.startsWith(prefix) || imageUrl.contains("\\")) continue;
            Path imagePath = root.resolve(imageUrl.substring(prefix.length())).normalize();
            if (!imagePath.startsWith(root)) continue;
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException exception) {
                log.warn("Unable to remove review image {}", imageUrl, exception);
            }
        }
    }

    private String imageExtension(byte[] bytes) {
        if (bytes.length >= 3
                && (bytes[0] & 0xff) == 0xff
                && (bytes[1] & 0xff) == 0xd8
                && (bytes[2] & 0xff) == 0xff) {
            return "jpg";
        }
        if (bytes.length >= 8
                && (bytes[0] & 0xff) == 0x89
                && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47
                && bytes[4] == 0x0d && bytes[5] == 0x0a && bytes[6] == 0x1a && bytes[7] == 0x0a) {
            return "png";
        }
        if (bytes.length >= 12
                && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return "webp";
        }
        throw new BadRequestException("Chỉ hỗ trợ ảnh JPG, PNG hoặc WebP");
    }

    private record PendingImage(String fileName, byte[] bytes) {
    }
}
