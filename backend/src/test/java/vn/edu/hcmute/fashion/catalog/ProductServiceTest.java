package vn.edu.hcmute.fashion.catalog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import vn.edu.hcmute.fashion.catalog.dto.CatalogDtos.VariantRequest;
import vn.edu.hcmute.fashion.catalog.dto.CatalogDtos.VariantResponse;
import vn.edu.hcmute.fashion.catalog.entity.*;
import vn.edu.hcmute.fashion.catalog.repository.*;
import vn.edu.hcmute.fashion.catalog.service.FileStorageService;
import vn.edu.hcmute.fashion.catalog.service.ProductService;
import vn.edu.hcmute.fashion.common.ApiException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock ProductRepository products;
    @Mock CategoryRepository categories;
    @Mock BrandRepository brands;
    @Mock ProductVariantRepository variants;
    @Mock ProductImageRepository images;
    @Mock FileStorageService storage;
    @InjectMocks ProductService service;

    private VariantRequest req(String sku, String size, String color) {
        return new VariantRequest(sku, size, color, new BigDecimal("100000"), 5);
    }

    @Test
    void addVariant_duplicateSku_isRejected() {
        when(products.findById(1L)).thenReturn(Optional.of(new Product()));
        when(variants.existsBySku("ABC-1")).thenReturn(true); // " abc-1 " được chuẩn hóa

        ApiException ex = assertThrows(ApiException.class, () -> service.addVariant(1L, req(" abc-1 ", "M", "Đen")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(variants, never()).save(any());
    }

    @Test
    void updateVariant_skuUsedByAnotherVariant_isRejected() {
        when(variants.findById(7L)).thenReturn(Optional.of(new ProductVariant()));
        when(variants.existsBySkuAndIdNot("ABC-1", 7L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> service.updateVariant(7L, req("ABC-1", "M", null)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void addVariant_nullOrBlankSizeAndColor_isAllowed() {
        when(products.findById(1L)).thenReturn(Optional.of(new Product()));
        when(variants.existsBySku("TOTE-1")).thenReturn(false);
        when(variants.save(any(ProductVariant.class))).thenAnswer(inv -> inv.getArgument(0));

        VariantResponse nullCase = service.addVariant(1L, req("tote-1", null, null));
        VariantResponse blankCase = service.addVariant(1L, req("tote-1", "  ", ""));

        assertNull(nullCase.size());
        assertNull(nullCase.color());
        assertNull(blankCase.size());
        assertNull(blankCase.color());
        assertEquals("TOTE-1", nullCase.sku());
        assertEquals(5, nullCase.stockQty());
    }

    @Test
    void getPublic_inactiveOrDeletedProduct_isNotFound() {
        for (ProductStatus s : new ProductStatus[] {ProductStatus.INACTIVE, ProductStatus.DELETED}) {
            Product p = new Product();
            p.setStatus(s);
            when(products.findById(5L)).thenReturn(Optional.of(p));

            ApiException ex = assertThrows(ApiException.class, () -> service.getPublic(5L));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        }
    }

    @Test
    void softDelete_setsStatusDeleted() {
        Product p = new Product();
        when(products.findById(3L)).thenReturn(Optional.of(p));

        service.softDelete(3L);

        assertEquals(ProductStatus.DELETED, p.getStatus());
    }
}
