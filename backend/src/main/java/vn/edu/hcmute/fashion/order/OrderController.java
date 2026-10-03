package vn.edu.hcmute.fashion.order;

import static vn.edu.hcmute.fashion.order.OrderDtos.*;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }
    @PostMapping public OrderDetail checkout(Authentication auth, @RequestBody Checkout body) { return service.checkout(auth.getName(), body); }
    @GetMapping public List<OrderSummary> list(Authentication auth) { return service.listOrders(auth.getName(), false); }
    @GetMapping("/{orderId}") public OrderDetail detail(Authentication auth, @PathVariable long orderId) { return service.orderDetail(auth.getName(), orderId, false); }
    @PutMapping("/{orderId}/cancel") public OrderDetail cancel(Authentication auth, @PathVariable long orderId) { return service.cancel(auth.getName(), orderId); }
}
