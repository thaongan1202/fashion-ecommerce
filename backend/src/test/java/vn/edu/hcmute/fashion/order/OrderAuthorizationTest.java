package vn.edu.hcmute.fashion.order;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.hcmute.fashion.shared.SecurityConfiguration;

@WebMvcTest(controllers = {CartController.class, AdminOrderController.class})
@Import({SecurityConfiguration.class, vn.edu.hcmute.fashion.review.ReviewImageStorageService.class})
class OrderAuthorizationTest {
    @Autowired MockMvc mvc;
    @MockitoBean OrderService service;
    @MockitoBean JwtDecoder jwtDecoder;

    @Test
    void guestCannotCallCartAndServiceIsNotReached() throws Exception {
        mvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void cartUsesUserIdFromJwtSubject() throws Exception {
        when(service.getCart(42L)).thenReturn(new OrderDtos.Cart(List.of(), BigDecimal.ZERO, 0));

        mvc.perform(get("/api/cart").with(jwt()
                        .jwt(token -> token.subject("42"))
                        .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isOk());

        verify(service).getCart(42L);
    }

    @Test
    void customerCannotCallAdminOrderApi() throws Exception {
        mvc.perform(get("/api/admin/orders").with(jwt()
                        .jwt(token -> token.subject("42").claim("role", "CUSTOMER"))
                        .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }
}
