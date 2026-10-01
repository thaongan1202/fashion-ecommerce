package vn.edu.hcmute.fashion.order;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class OrderErrorHandler {
    @ExceptionHandler(OrderException.class)
    ResponseEntity<Map<String, Object>> handle(OrderException error) {
        return ResponseEntity.status(error.getStatus()).body(Map.of(
                "status", error.getStatus().value(), "code", error.getCode(), "message", error.getMessage()));
    }
}
