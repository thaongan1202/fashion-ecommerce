package vn.edu.hcmute.fashion.review;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import vn.edu.hcmute.fashion.shared.DemoAuthController;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/reviews")
public class ReviewReminderController {
    private final ReviewRepository repository;

    public ReviewReminderController(ReviewRepository repository) { this.repository = repository; }

    @GetMapping("/to-review")
    public List<ReviewReminder> toReview(HttpSession session) {
        Object userId = session.getAttribute(DemoAuthController.SESSION_USER_ID);
        if (userId instanceof Long id) return repository.findDeliveredProductsWithoutReview(id);
        throw new ResponseStatusException(UNAUTHORIZED, "Đăng nhập để xem lời nhắc đánh giá");
    }
}
