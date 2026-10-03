package vn.edu.hcmute.fashion.order;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

final class OrderPrincipal {
    private OrderPrincipal() {}

    static long userId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new OrderException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Vui lòng đăng nhập.");
        }
        try {
            long userId = Long.parseLong(jwt.getSubject());
            if (userId <= 0) throw new NumberFormatException("user id must be positive");
            return userId;
        } catch (NumberFormatException exception) {
            throw new OrderException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN_SUBJECT", "Token không chứa userId hợp lệ.");
        }
    }
}
