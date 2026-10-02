package vn.edu.hcmute.fashion.review;

import static vn.edu.hcmute.fashion.review.AdminReviewDtos.*;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {
    private final ReviewModerationService service;
    public AdminReviewController(ReviewModerationService service) { this.service = service; }

    @GetMapping
    public ReviewPage list(Authentication auth, @RequestParam(defaultValue = "PENDING") String status,
                           @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.list(auth == null ? null : auth.getName(), status, page, size);
    }

    @PutMapping("/{reviewId}/status")
    public ReviewItem status(Authentication auth, @PathVariable long reviewId, @RequestBody StatusRequest request) {
        return service.changeStatus(auth == null ? null : auth.getName(), reviewId, request == null ? null : request.status());
    }
}
