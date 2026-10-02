package vn.edu.hcmute.fashion.review;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.hcmute.fashion.shared.DemoAuthController;

@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {
    private final ReviewService service;

    public AdminReviewController(ReviewService service) { this.service = service; }

    @GetMapping
    public java.util.List<AdminReviewResponse> pending(HttpSession session) {
        return service.listPending(requireUserId(session));
    }

    @PatchMapping("/{reviewId}/status")
    public AdminReviewResponse changeStatus(@PathVariable long reviewId, HttpSession session,
            @Valid @RequestBody ChangeReviewStatus request) {
        return service.changeStatus(requireUserId(session), reviewId, request.status().name());
    }

    private long requireUserId(HttpSession session) {
        Object value = session.getAttribute(DemoAuthController.SESSION_USER_ID);
        if (value instanceof Long id) return id;
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "Đăng nhập Admin để quản lý review");
    }

    public record ChangeReviewStatus(@NotNull ReviewStatus status) {}
    public enum ReviewStatus { APPROVED, HIDDEN }
}
