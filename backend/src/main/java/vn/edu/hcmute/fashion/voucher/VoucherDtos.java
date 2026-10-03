package vn.edu.hcmute.fashion.voucher;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public final class VoucherDtos {
    private VoucherDtos() {}

    public record ApplyRequest(@NotBlank @Size(max = 60) String code) {}
    public record ApplyResponse(boolean valid, String code, BigDecimal discountAmount,
                                BigDecimal totalAfterDiscount, String message) {}
    public record VoucherRequest(
            @NotBlank @Size(max = 60) @Pattern(regexp = "[A-Za-z0-9_-]+") String code,
            @NotNull DiscountType discountType,
            @NotNull @DecimalMin("0.01") @DecimalMax("10000000000.00") BigDecimal discountValue,
            @NotNull @DecimalMin("0.00") @DecimalMax("10000000000.00") BigDecimal minOrderValue,
            @NotNull LocalDate expiryDate,
            @NotNull @Min(1) @Max(1000000000) Integer usageLimit,
            boolean active) {}

    public record VoucherResponse(Long id, String code, DiscountType discountType, BigDecimal discountValue,
                                  BigDecimal minOrderValue, LocalDate expiryDate, Integer usageLimit,
                                  Integer usedCount, boolean active, OffsetDateTime createdAt) {}
    public enum DiscountType {
        PERCENT, FIXED;
        public String value() { return name(); }
        public static DiscountType from(String value) { return valueOf(value); }
    }
}
