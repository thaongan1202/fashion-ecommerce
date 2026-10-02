package vn.edu.hcmute.fashion.shared;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AdminExceptionHandler {
    @ExceptionHandler(AdminException.class)
    public ResponseEntity<ApiError> handleAdmin(AdminException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(new ApiError(exception.getStatus().value(), exception.getCode(), exception.getMessage(), null));
    }

    public record ApiError(int status, String code, String message, Map<String, String> fieldErrors) {}
}
