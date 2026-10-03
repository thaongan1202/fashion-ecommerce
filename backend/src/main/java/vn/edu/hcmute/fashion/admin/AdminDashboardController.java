package vn.edu.hcmute.fashion.admin;

import static vn.edu.hcmute.fashion.admin.AdminDashboardDtos.*;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {
    private final AdminDashboardService service;
    public AdminDashboardController(AdminDashboardService service) { this.service = service; }
    @GetMapping
    public DashboardSummary get(Authentication auth) { return service.get(auth == null ? null : auth.getName()); }
}
