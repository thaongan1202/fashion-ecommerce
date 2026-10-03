package vn.edu.hcmute.fashion.catalog.service;

import vn.edu.hcmute.fashion.common.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Lưu ảnh local. Chỉ nhận JPG/PNG/WEBP, tối đa 5MB (cấu hình ở application.properties). */
@Service
public class FileStorageService {
    private static final Map<String, String> TYPES =
            Map.of("image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp");

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String dir) throws IOException {
        this.root = Path.of(dir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    /** @return URL công khai dạng /uploads/xxx.jpg */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) throw ApiException.badRequest("Chưa chọn file ảnh");
        String ext = TYPES.get(file.getContentType());
        if (ext == null) throw ApiException.badRequest("Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP");
        String name = UUID.randomUUID() + ext;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, root.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Không lưu được ảnh");
        }
        return "/uploads/" + name;
    }

    public void delete(String url) {
        if (url == null || !url.startsWith("/uploads/")) return;
        Path p = root.resolve(url.substring("/uploads/".length())).normalize();
        if (!p.startsWith(root)) return;
        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
    }
}
