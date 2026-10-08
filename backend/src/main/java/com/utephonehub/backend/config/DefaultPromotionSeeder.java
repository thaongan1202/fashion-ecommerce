package com.utephonehub.backend.config;

import com.utephonehub.backend.entity.Promotion;
import com.utephonehub.backend.entity.PromotionTemplate;
import com.utephonehub.backend.enums.EPromotionStatus;
import com.utephonehub.backend.enums.EPromotionTemplateType;
import com.utephonehub.backend.repository.PromotionRepository;
import com.utephonehub.backend.repository.PromotionTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultPromotionSeeder implements ApplicationRunner {

    private final PromotionRepository promotionRepository;
    private final PromotionTemplateRepository templateRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        PromotionTemplate voucherTemplate = ensureTemplate(
                "template-003", "VOUCHER_TEMPLATE", EPromotionTemplateType.VOUCHER);
        PromotionTemplate freeshipTemplate = ensureTemplate(
                "template-002", "FREESHIP_TEMPLATE", EPromotionTemplateType.FREESHIP);

        seed(voucherTemplate, "promo-sale10", "SALE10", "Giảm 10%",
                "Giảm 10% cho mọi đơn, tối đa 200.000đ",
                10.0, null, 200000.0, 0.0);
        seed(voucherTemplate, "promo-giam50k", "GIAM50K", "Giảm 50.000đ",
                "Giảm 50.000đ cho đơn từ 200.000đ",
                null, 50000.0, null, 200000.0);
        seed(freeshipTemplate, "promo-freeship", "FREESHIP", "Miễn phí vận chuyển",
                "Miễn phí vận chuyển cho mọi đơn",
                null, null, null, 0.0);
    }

    private PromotionTemplate ensureTemplate(String id, String code, EPromotionTemplateType type) {
        return templateRepository.findByCode(code).orElseGet(() -> templateRepository.save(
                PromotionTemplate.builder()
                        .id(id)
                        .code(code)
                        .type(type)
                        .createdAt(LocalDateTime.now())
                        .build()));
    }

    private void seed(
            PromotionTemplate template,
            String id,
            String code,
            String title,
            String description,
            Double percent,
            Double fixedAmount,
            Double maxDiscount,
            Double minValue) {
        if (promotionRepository.existsByCodeIgnoreCase(code) || promotionRepository.existsById(id)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        promotionRepository.save(Promotion.builder()
                .id(id)
                .code(code)
                .title(title)
                .description(description)
                .effectiveDate(now.minusDays(1))
                .expirationDate(now.plusYears(2))
                .percentDiscount(percent)
                .fixedAmount(fixedAmount)
                .maxDiscount(maxDiscount)
                .minValueToBeApplied(minValue)
                .status(EPromotionStatus.ACTIVE)
                .template(template)
                .targets(new ArrayList<>())
                .build());
        log.info("Seeded default promotion code {}", code);
    }
}
