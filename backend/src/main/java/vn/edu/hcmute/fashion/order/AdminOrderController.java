package vn.edu.hcmute.fashion.order;

import static vn.edu.hcmute.fashion.order.OrderDtos.*;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final OrderService service;
    public AdminOrderController(OrderService service) { this.service = service; }
    @GetMapping public List<OrderSummary> list(@AuthenticationPrincipal Jwt jwt) { return service.listOrders(OrderPrincipal.userId(jwt), true); }
    @GetMapping("/{orderId}") public OrderDetail detail(@AuthenticationPrincipal Jwt jwt, @PathVariable long orderId) { return service.orderDetail(OrderPrincipal.userId(jwt), orderId, true); }
    @PutMapping("/{orderId}/status") public OrderDetail status(@AuthenticationPrincipal Jwt jwt, @PathVariable long orderId, @RequestBody StatusChange body) { return service.changeStatus(OrderPrincipal.userId(jwt), orderId, body); }
}
