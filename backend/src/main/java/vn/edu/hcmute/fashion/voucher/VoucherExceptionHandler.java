package vn.edu.hcmute.fashion.voucher;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class VoucherExceptionHandler {
    @ExceptionHandler(VoucherException.class)
    public ResponseEntity<ApiError> handleVoucher(VoucherException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(new ApiError(exception.getStatus().value(), exception.getCode(), exception.getMessage(), null));
    }

    public record ApiError(int status, String code, String message, Map<String, String> fieldErrors) {}
}
