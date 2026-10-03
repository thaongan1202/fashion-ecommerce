package vn.edu.hcmute.fashion.order;

import static vn.edu.hcmute.fashion.order.OrderDtos.*;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }
    @PostMapping public OrderDetail checkout(@AuthenticationPrincipal Jwt jwt, @RequestBody Checkout body) { return service.checkout(OrderPrincipal.userId(jwt), body); }
    @GetMapping public List<OrderSummary> list(@AuthenticationPrincipal Jwt jwt) { return service.listOrders(OrderPrincipal.userId(jwt), false); }
    @GetMapping("/{orderId}") public OrderDetail detail(@AuthenticationPrincipal Jwt jwt, @PathVariable long orderId) { return service.orderDetail(OrderPrincipal.userId(jwt), orderId, false); }
    @PutMapping("/{orderId}/cancel") public OrderDetail cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable long orderId) { return service.cancel(OrderPrincipal.userId(jwt), orderId); }
}
