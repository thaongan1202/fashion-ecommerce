package vn.edu.hcmute.fashion.catalog;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import vn.edu.hcmute.fashion.catalog.entity.Brand;
import vn.edu.hcmute.fashion.catalog.entity.Category;
import vn.edu.hcmute.fashion.catalog.repository.*;
import vn.edu.hcmute.fashion.catalog.service.CatalogService;
import vn.edu.hcmute.fashion.common.ApiException;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {
    @Mock CategoryRepository categories;
    @Mock BrandRepository brands;
    @Mock ProductRepository products;
    @InjectMocks CatalogService service;

    @Test
    void deleteCategory_stillHasProducts_isRejected() {
        when(categories.findById(1L)).thenReturn(Optional.of(new Category()));
        when(products.existsByCategoryId(1L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> service.deleteCategory(1L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(categories, never()).delete(any());
    }

    @Test
    void deleteCategory_noProducts_isDeleted() {
        Category c = new Category();
        when(categories.findById(1L)).thenReturn(Optional.of(c));
        when(products.existsByCategoryId(1L)).thenReturn(false);

        service.deleteCategory(1L);

        verify(categories).delete(c);
    }

    @Test
    void deleteBrand_stillHasProducts_isRejected() {
        when(brands.findById(2L)).thenReturn(Optional.of(new Brand()));
        when(products.existsByBrandId(2L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> service.deleteBrand(2L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(brands, never()).delete(any());
    }

    @Test
    void deleteBrand_noProducts_isDeleted() {
        Brand b = new Brand();
        when(brands.findById(2L)).thenReturn(Optional.of(b));
        when(products.existsByBrandId(2L)).thenReturn(false);

        service.deleteBrand(2L);

        verify(brands).delete(b);
    }
}
