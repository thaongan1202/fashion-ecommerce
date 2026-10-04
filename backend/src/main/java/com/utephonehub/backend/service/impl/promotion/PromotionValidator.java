package com.utephonehub.backend.service.impl.promotion;

import com.utephonehub.backend.entity.Promotion;
import com.utephonehub.backend.enums.EPromotionStatus;
import com.utephonehub.backend.exception.promotion.PromotionInvalidException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Validator for Promotion business rules
 * Follows Single Responsibility Principle (SRP) - only handles validation
 * Follows Information Expert (GRASP) - knows promotion validation rules
 */
@Component
public class PromotionValidator {

    /**
     * Validate if promotion can be applied
     * @param promotion Promotion to validate
     * @param orderTotal Order total amount
     * @throws PromotionInvalidException if validation fails
     */
    public void validatePromotionApplicability(Promotion promotion, Double orderTotal) {
        validatePromotionActive(promotion);
        validatePromotionDateRange(promotion);
        validateMinimumOrderValue(promotion, orderTotal);
    }

    /**
     * Validate if promotion is active
     */
    public void validatePromotionActive(Promotion promotion) {
        if (promotion.getStatus() != EPromotionStatus.ACTIVE) {
            throw new PromotionInvalidException("Voucher không khả thi: Khuyến mãi không còn hoạt động");
        }
    }

    /**
     * Validate if promotion is within valid date range
     */
    public void validatePromotionDateRange(Promotion promotion) {
        LocalDateTime now = LocalDateTime.now();
        if (promotion.getEffectiveDate() == null || now.isBefore(promotion.getEffectiveDate())) {
            throw new PromotionInvalidException("Voucher không khả thi: Khuyến mãi chưa bắt đầu");
        }
        // The expiration instant is exclusive: it cannot be applied at or after that time.
        if (promotion.getExpirationDate() == null || !now.isBefore(promotion.getExpirationDate())) {
            throw new PromotionInvalidException("Voucher không khả thi: Khuyến mãi đã hết hạn");
        }
    }

    /**
     * Validate if order total meets minimum required value
     */
    public void validateMinimumOrderValue(Promotion promotion, Double orderTotal) {
        if (orderTotal == null || !Double.isFinite(orderTotal) || orderTotal < 0) {
            throw new PromotionInvalidException("Voucher không khả thi: Giá trị đơn hàng không hợp lệ");
        }
        if (promotion.getMinValueToBeApplied() != null 
                && orderTotal < promotion.getMinValueToBeApplied()) {
            throw new PromotionInvalidException(
                String.format("Voucher không khả thi: Đơn hàng chưa đạt giá trị tối thiểu %,.0fđ (hiện tại: %,.0fđ)",
                    promotion.getMinValueToBeApplied(), orderTotal)
            );
        }
    }

    /**
     * Check if promotion is currently valid (active and within date range)
     * Used for filtering available promotions
     */
    public boolean isPromotionValid(Promotion promotion, Double orderTotal) {
        try {
            validatePromotionApplicability(promotion, orderTotal);
            return true;
        } catch (PromotionInvalidException e) {
            return false;
        }
    }
}
