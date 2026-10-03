package vn.edu.hcmute.fashion.shared;

import java.util.List;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/demo")
public class DemoAuthController {
    public static final String SESSION_USER_ID = "demoCustomerId";
    private static final String DEMO_ADMIN_EMAIL = "demo.admin@fashion.local";
    private final JdbcTemplate jdbc;

    public DemoAuthController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/users")
    public List<DemoUser> users() {
        return jdbc.query("""
                SELECT id, full_name, email, role
                FROM users
                WHERE ((email LIKE 'demo.%@fashion.local' AND role = 'CUSTOMER') OR email = ? AND role = 'ADMIN')
                  AND status = 'ACTIVE'
                ORDER BY id
                """, (rs, row) -> new DemoUser(rs.getLong("id"), rs.getString("full_name"),
                rs.getString("email"), rs.getString("role")), DEMO_ADMIN_EMAIL);
    }

    @PostMapping("/login")
    public DemoSession login(@RequestBody DemoLogin request, HttpServletRequest servletRequest,
            HttpServletResponse servletResponse, HttpSession session) {
        var user = jdbc.query("""
                SELECT id, full_name, email, role
                FROM users
                WHERE email = lower(trim(?)) AND status = 'ACTIVE'
                  AND ((role = 'CUSTOMER' AND email LIKE 'demo.%@fashion.local')
                       OR (role = 'ADMIN' AND email = ?))
                """, (rs, row) -> new DemoUser(rs.getLong("id"), rs.getString("full_name"),
                rs.getString("email"), rs.getString("role")), request.email(), DEMO_ADMIN_EMAIL)
                .stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản demo không hợp lệ"));
        session.setAttribute(SESSION_USER_ID, user.id());
        var authentication = new UsernamePasswordAuthenticationToken(user.id().toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.role())));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context, servletRequest, servletResponse);
        return new DemoSession(user.id(), user.fullName(), user.email(), user.role());
    }

    @GetMapping("/session")
    public DemoSession currentSession(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return new DemoSession(null, null, null, null);
        return jdbc.query("""
                SELECT id, full_name, email, role FROM users
                WHERE id = ? AND status = 'ACTIVE'
                  AND (role = 'CUSTOMER' OR (role = 'ADMIN' AND email = ?))
                """, (rs, row) -> new DemoSession(rs.getLong("id"), rs.getString("full_name"),
                rs.getString("email"), rs.getString("role")), userId, DEMO_ADMIN_EMAIL)
                .stream().findFirst().orElseGet(() -> new DemoSession(null, null, null, null));
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) {
        SecurityContextHolder.clearContext();
        session.invalidate();
    }

    public record DemoLogin(String email) {}
    public record DemoUser(Long id, String fullName, String email, String role) {}
    public record DemoSession(Long userId, String fullName, String email, String role) {}
}
