package vn.edu.hcmute.fashion.shared;

import java.util.List;
import jakarta.servlet.http.HttpSession;
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
    private final JdbcTemplate jdbc;

    public DemoAuthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/users")
    public List<DemoUser> users() {
        return jdbc.query("""
                SELECT id, full_name, email
                FROM users
                WHERE email LIKE 'demo.%@fashion.local'
                  AND role = 'CUSTOMER' AND status = 'ACTIVE'
                ORDER BY id
                """, (rs, row) -> new DemoUser(rs.getLong("id"), rs.getString("full_name"), rs.getString("email")));
    }

    @PostMapping("/login")
    public DemoSession login(@RequestBody DemoLogin request, HttpSession session) {
        var user = jdbc.query("""
                SELECT id, full_name, email
                FROM users
                WHERE email = lower(trim(?))
                  AND email LIKE 'demo.%@fashion.local'
                  AND role = 'CUSTOMER' AND status = 'ACTIVE'
                """, (rs, row) -> new DemoUser(rs.getLong("id"), rs.getString("full_name"), rs.getString("email")), request.email())
                .stream().findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản demo không hợp lệ"));
        session.setAttribute(SESSION_USER_ID, user.id());
        return new DemoSession(user.id(), user.fullName(), user.email());
    }

    @GetMapping("/session")
    public DemoSession currentSession(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) return new DemoSession(null, null, null);
        return jdbc.query("SELECT id, full_name, email FROM users WHERE id = ? AND status = 'ACTIVE' AND role = 'CUSTOMER'",
                (rs, row) -> new DemoSession(rs.getLong("id"), rs.getString("full_name"), rs.getString("email")), userId)
                .stream().findFirst().orElseGet(() -> new DemoSession(null, null, null));
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }

    public record DemoLogin(String email) {}
    public record DemoUser(Long id, String fullName, String email) {}
    public record DemoSession(Long userId, String fullName, String email) {}
}
