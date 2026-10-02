package vn.edu.hcmute.fashion.voucher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static vn.edu.hcmute.fashion.voucher.VoucherDtos.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class VoucherRulesTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 1);

    @Test
    void percentageDiscountIsRoundedToTwoDecimals() {
        assertThat(VoucherService.calculateDiscount("PERCENT", new BigDecimal("12.5"), new BigDecimal("99999")))
                .isEqualByComparingTo("12499.88");
    }

    @Test
    void discountCannotExceedSubtotal() {
        assertThat(VoucherService.calculateDiscount("FIXED", new BigDecimal("150000"), new BigDecimal("80000")))
                .isEqualByComparingTo("80000.00");
    }

    @Test
    void rejectsExpiredVoucher() {
        assertThatThrownBy(() -> VoucherService.ensureUsable(voucher(true, LocalDate.of(2026, 9, 30), 0, 5, 0),
                new BigDecimal("50000"), TODAY))
                .isInstanceOf(VoucherException.class).hasMessageContaining("hết hạn");
    }

    @Test
    void rejectsVoucherWhenUsageLimitIsReached() {
        assertThatThrownBy(() -> VoucherService.ensureUsable(voucher(true, TODAY, 5, 5, 0),
                new BigDecimal("50000"), TODAY))
                .isInstanceOf(VoucherException.class).hasMessageContaining("hết lượt");
    }

    @Test
    void rejectsOrderBelowMinimum() {
        assertThatThrownBy(() -> VoucherService.ensureUsable(voucher(true, TODAY, 0, 5, 100000),
                new BigDecimal("99999"), TODAY))
                .isInstanceOf(VoucherException.class).hasMessageContaining("tối thiểu");
    }

    private VoucherResponse voucher(boolean active, LocalDate expiry, int used, int limit, long minimum) {
        return new VoucherResponse(1L, "TEST", DiscountType.PERCENT, new BigDecimal("10"), BigDecimal.valueOf(minimum),
                expiry, limit, used, active, OffsetDateTime.parse("2026-10-01T00:00:00Z"));
    }
}
