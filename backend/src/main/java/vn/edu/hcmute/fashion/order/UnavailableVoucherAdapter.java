package vn.edu.hcmute.fashion.order;

import java.math.BigDecimal;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
class UnavailableVoucherAdapter {
    @Bean
    @ConditionalOnMissingBean(CheckoutVoucherPort.class)
    CheckoutVoucherPort unavailableVoucherPort() {
        return (code, subtotal) -> { throw new OrderException(HttpStatus.SERVICE_UNAVAILABLE,
                "VOUCHER_UNAVAILABLE", "Chức năng voucher chưa được tích hợp."); };
    }
}
