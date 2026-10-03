package vn.edu.hcmute.fashion.review;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReviewImageStorageService {
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final Pattern STORED_IMAGE_URL = Pattern.compile("^/uploads/reviews/([0-9a-fA-F-]{36}\\.(?:jpg|png|webp))$");
    private final Path storageDirectory;

    public ReviewImageStorageService(@Value("${app.upload.review-dir:uploads/reviews}") String directory) {
        this.storageDirectory = Path.of(directory).toAbsolutePath().normalize();
    }

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng chọn ảnh đánh giá");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ảnh đánh giá không được vượt quá 5 MB");
        }

        try {
            byte[] bytes = file.getBytes();
            String extension = imageExtension(bytes);
            String filename = UUID.randomUUID() + extension;
            Files.createDirectories(storageDirectory);
            Files.write(storageDirectory.resolve(filename), bytes, StandardOpenOption.CREATE_NEW);
            return "/uploads/reviews/" + filename;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể lưu ảnh đánh giá");
        }
    }

    public Path storageDirectory() { return storageDirectory; }

    public boolean isStoredImage(String imageUrl) {
        Matcher matcher = STORED_IMAGE_URL.matcher(imageUrl);
        return matcher.matches() && Files.isRegularFile(storageDirectory.resolve(matcher.group(1)));
    }

    private String imageExtension(byte[] bytes) {
        if (bytes.length >= 3 && unsigned(bytes[0]) == 0xff && unsigned(bytes[1]) == 0xd8 && unsigned(bytes[2]) == 0xff) return ".jpg";
        if (bytes.length >= 8 && unsigned(bytes[0]) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e
                && bytes[3] == 0x47 && bytes[4] == 0x0d && bytes[5] == 0x0a && bytes[6] == 0x1a && bytes[7] == 0x0a) return ".png";
        if (bytes.length >= 12 && ascii(bytes, 0, "RIFF") && ascii(bytes, 8, "WEBP")) return ".webp";
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ chấp nhận ảnh JPEG, PNG hoặc WebP hợp lệ");
    }

    private int unsigned(byte value) { return value & 0xff; }

    private boolean ascii(byte[] bytes, int offset, String value) {
        for (int i = 0; i < value.length(); i++) if (bytes[offset + i] != value.charAt(i)) return false;
        return true;
    }
}
