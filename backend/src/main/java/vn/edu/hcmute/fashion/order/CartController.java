package vn.edu.hcmute.fashion.order;

import static vn.edu.hcmute.fashion.order.OrderDtos.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final OrderService service;
    public CartController(OrderService service) { this.service = service; }
    @GetMapping public Cart get(@AuthenticationPrincipal Jwt jwt) { return service.getCart(OrderPrincipal.userId(jwt)); }
    @PostMapping("/items") public Cart add(@AuthenticationPrincipal Jwt jwt, @RequestBody AddCartItem body) { return service.addItem(OrderPrincipal.userId(jwt), body); }
    @PutMapping("/items/{itemId}") public Cart update(@AuthenticationPrincipal Jwt jwt, @PathVariable long itemId, @RequestBody UpdateCartItem body) { return service.updateItem(OrderPrincipal.userId(jwt), itemId, body); }
    @DeleteMapping("/items/{itemId}") public Cart remove(@AuthenticationPrincipal Jwt jwt, @PathVariable long itemId) { return service.removeItem(OrderPrincipal.userId(jwt), itemId); }
}
