package vn.edu.hcmute.fashion.voucher;

import static vn.edu.hcmute.fashion.voucher.VoucherDtos.*;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/vouchers")
public class AdminVoucherController {
    private final VoucherService service;
    public AdminVoucherController(VoucherService service) { this.service = service; }

    @GetMapping
    public List<VoucherResponse> list(Authentication auth) { return service.list(auth == null ? null : auth.getName()); }

    @GetMapping("/{voucherId}")
    public VoucherResponse get(Authentication auth, @PathVariable long voucherId) {
        return service.get(auth == null ? null : auth.getName(), voucherId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VoucherResponse create(Authentication auth, @Valid @RequestBody VoucherRequest request) {
        return service.create(auth == null ? null : auth.getName(), request);
    }

    @PutMapping("/{voucherId}")
    public VoucherResponse update(Authentication auth, @PathVariable long voucherId, @Valid @RequestBody VoucherRequest request) {
        return service.update(auth == null ? null : auth.getName(), voucherId, request);
    }

    @DeleteMapping("/{voucherId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable long voucherId) {
        service.delete(auth == null ? null : auth.getName(), voucherId);
    }
}
