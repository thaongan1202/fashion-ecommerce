package vn.edu.hcmute.fashion.voucher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import vn.edu.hcmute.fashion.order.CheckoutVoucherPort;

@Configuration
public class VoucherCheckoutConfiguration {
    @Bean
    @Primary
    CheckoutVoucherPort checkoutVoucherPort(VoucherService vouchers) {
        return vouchers::redeem;
    }
}
