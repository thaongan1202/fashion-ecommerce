package vn.edu.hcmute.fashion.order;

import static vn.edu.hcmute.fashion.order.OrderDtos.*;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final OrderService service;
    public AdminOrderController(OrderService service) { this.service = service; }
    @GetMapping public List<OrderSummary> list(Authentication auth) { return service.listOrders(auth.getName(), true); }
    @GetMapping("/{orderId}") public OrderDetail detail(Authentication auth, @PathVariable long orderId) { return service.orderDetail(auth.getName(), orderId, true); }
    @PutMapping("/{orderId}/status") public OrderDetail status(Authentication auth, @PathVariable long orderId, @RequestBody StatusChange body) { return service.changeStatus(auth.getName(), orderId, body); }
}
