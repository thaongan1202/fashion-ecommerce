package vn.edu.hcmute.fashion.catalog;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.edu.hcmute.fashion.catalog.controller.AdminCatalogController;
import vn.edu.hcmute.fashion.catalog.controller.PublicCatalogController;
import vn.edu.hcmute.fashion.catalog.dto.CatalogDtos.VariantResponse;
import vn.edu.hcmute.fashion.catalog.service.CatalogService;
import vn.edu.hcmute.fashion.catalog.service.ProductService;
import vn.edu.hcmute.fashion.common.ApiException;
import vn.edu.hcmute.fashion.common.GlobalExceptionHandler;
import vn.edu.hcmute.fashion.common.PageResponse;

/** Test URL /api/** và mã lỗi, không cần DB/security (standaloneSetup). */
@ExtendWith(MockitoExtension.class)
class CatalogControllerTest {
    @Mock CatalogService catalog;
    @Mock ProductService products;
    MockMvc publicMvc;
    MockMvc adminMvc;

    @BeforeEach
    void setUp() {
        publicMvc = MockMvcBuilders.standaloneSetup(new PublicCatalogController(catalog, products))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        adminMvc = MockMvcBuilders.standaloneSetup(new AdminCatalogController(catalog, products))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void publicDetail_hiddenProduct_returns404() throws Exception {
        when(products.getPublic(9L)).thenThrow(ApiException.notFound("Sản phẩm không tồn tại"));

        publicMvc.perform(get("/api/products/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Sản phẩm không tồn tại"));
    }

    @Test
    void publicList_passesFiltersToService() throws Exception {
        when(products.listPublic(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(), 0, 12, 0, 0));

        publicMvc.perform(get("/api/products")
                        .param("keyword", "ao").param("categoryId", "1").param("minPrice", "100000"))
                .andExpect(status().isOk());

        verify(products).listPublic("ao", 1L, null, new BigDecimal("100000"), null, 0, 12);
    }

    @Test
    void adminCreateVariant_withoutSizeAndColor_returns201() throws Exception {
        when(products.addVariant(eq(1L), any()))
                .thenReturn(new VariantResponse(10L, "TOTE-1", null, null, new BigDecimal("150000"), 50));

        adminMvc.perform(post("/api/admin/products/1/variants").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"TOTE-1\",\"price\":150000,\"stockQty\":50}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.size").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.color").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void adminCreateVariant_negativeStock_returns400() throws Exception {
        adminMvc.perform(post("/api/admin/products/1/variants").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"X1\",\"price\":1000,\"stockQty\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.stockQty").exists());
    }
}
