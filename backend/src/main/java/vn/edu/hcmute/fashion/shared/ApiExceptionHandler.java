package vn.edu.hcmute.fashion.shared;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleStatus(ResponseStatusException exception) {
        int status = exception.getStatusCode().value();
        String message = exception.getReason() == null ? "Yêu cầu không hợp lệ" : exception.getReason();
        return ResponseEntity.status(status).body(new ApiError(status, code(status), message, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(error -> error.getField(), error -> error.getDefaultMessage() == null ? "Giá trị không hợp lệ" : error.getDefaultMessage(), (first, ignored) -> first));
        return ResponseEntity.badRequest().body(new ApiError(400, "VALIDATION_ERROR", "Dữ liệu gửi lên không hợp lệ", errors));
    }

    private String code(int status) {
        return switch (HttpStatus.valueOf(status)) {
            case NOT_FOUND -> "NOT_FOUND";
            case FORBIDDEN -> "FORBIDDEN";
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case CONFLICT -> "CONFLICT";
            case BAD_REQUEST -> "VALIDATION_ERROR";
            default -> "REQUEST_ERROR";
        };
    }

    public record ApiError(int status, String code, String message, Map<String, String> fieldErrors) {}
}
