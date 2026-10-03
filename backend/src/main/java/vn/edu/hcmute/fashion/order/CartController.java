package vn.edu.hcmute.fashion.order;

import static vn.edu.hcmute.fashion.order.OrderDtos.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final OrderService service;
    public CartController(OrderService service) { this.service = service; }
    @GetMapping public Cart get(Authentication auth) { return service.getCart(auth.getName()); }
    @PostMapping("/items") public Cart add(Authentication auth, @RequestBody AddCartItem body) { return service.addItem(auth.getName(), body); }
    @PutMapping("/items/{itemId}") public Cart update(Authentication auth, @PathVariable long itemId, @RequestBody UpdateCartItem body) { return service.updateItem(auth.getName(), itemId, body); }
    @DeleteMapping("/items/{itemId}") public Cart remove(Authentication auth, @PathVariable long itemId) { return service.removeItem(auth.getName(), itemId); }
}
