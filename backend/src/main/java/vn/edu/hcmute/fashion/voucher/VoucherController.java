package vn.edu.hcmute.fashion.voucher;

import static vn.edu.hcmute.fashion.voucher.VoucherDtos.*;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {
    private final VoucherService service;

    public VoucherController(VoucherService service) { this.service = service; }

    @PostMapping("/apply")
    public ApplyResponse apply(Authentication authentication, @Valid @RequestBody ApplyRequest request) {
        // Apply validates and previews only; used_count is incremented by redeem() inside checkout.
        return service.preview(authentication == null ? null : authentication.getName(), request.code());
    }
}
