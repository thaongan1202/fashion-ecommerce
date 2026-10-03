package vn.edu.hcmute.fashion.admin;

import static vn.edu.hcmute.fashion.admin.AdminUserDtos.*;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminUserService service;
    public AdminUserController(AdminUserService service) { this.service = service; }

    @GetMapping
    public UserPage list(Authentication auth, @RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String role, @RequestParam(required = false) String status,
                         @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.list(auth == null ? null : auth.getName(), keyword, role, status, page, size);
    }

    @PutMapping("/{userId}/status")
    public UserSummary status(Authentication auth, @PathVariable long userId, @RequestBody UserStatusRequest request) {
        return service.changeStatus(auth == null ? null : auth.getName(), userId, request == null ? null : request.status());
    }
}
