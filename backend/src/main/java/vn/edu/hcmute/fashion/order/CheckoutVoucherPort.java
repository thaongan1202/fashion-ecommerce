package vn.edu.hcmute.fashion.order;

import java.math.BigDecimal;

/** Integration boundary owned by Member 5. Implementations must join the caller's transaction. */
public interface CheckoutVoucherPort {
    BigDecimal redeem(String code, BigDecimal subtotal);
}
