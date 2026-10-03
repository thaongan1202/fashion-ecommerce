package vn.edu.hcmute.fashion.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class OrderPrincipalTest {
    @Test
    void readsNumericUserIdFromJwtSubject() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("42")
                .claim("email", "not-used@example.test")
                .build();

        assertEquals(42L, OrderPrincipal.userId(jwt));
    }

    @Test
    void rejectsNonNumericJwtSubject() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("customer@example.test")
                .build();

        OrderException error = assertThrows(OrderException.class, () -> OrderPrincipal.userId(jwt));
        assertEquals("INVALID_TOKEN_SUBJECT", error.getCode());
    }
}
