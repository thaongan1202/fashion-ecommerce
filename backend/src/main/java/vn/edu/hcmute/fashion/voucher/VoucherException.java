package vn.edu.hcmute.fashion.voucher;

import org.springframework.http.HttpStatus;

public class VoucherException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public VoucherException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
}
