package com.utephonehub.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ProductMetadata Entity - thông tin thời trang của sản phẩm.
 * Product 1-1 ProductMetadata.
 */
@Entity
@Table(name = "product_metadata", indexes = {
    @Index(name = "idx_product_metadata_product_id", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProductMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", unique = true, nullable = false)
    private Product product;

    @Column(precision = 15, scale = 2)
    private BigDecimal importPrice;

    @Column(precision = 15, scale = 2)
    private BigDecimal salePrice;

    @Column(length = 100)
    private String material;

    @Column(length = 100)
    private String style;

    @Column(name = "target_audience", length = 50)
    private String targetAudience;

    @Column(length = 50)
    private String season;

    @Column(length = 100)
    private String pattern;

    @Column(length = 100)
    private String fit;

    @Column(length = 100)
    private String origin;

    @Column(name = "care_instructions", columnDefinition = "TEXT")
    private String careInstructions;

    @Column(columnDefinition = "TEXT")
    private String additionalSpecs;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
