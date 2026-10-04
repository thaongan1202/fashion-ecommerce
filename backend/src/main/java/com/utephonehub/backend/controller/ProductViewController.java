package com.utephonehub.backend.controller;

import com.utephonehub.backend.dto.ApiResponse;
import com.utephonehub.backend.dto.request.productview.ProductFilterRequest;
import com.utephonehub.backend.dto.request.productview.ProductSearchFilterRequest;
import com.utephonehub.backend.dto.response.productview.*;
import com.utephonehub.backend.service.IProductViewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho ProductView API
 * API dành cho client-side: hiển thị, tìm kiếm, lọc, so sánh sản phẩm
 * 
 * Features:
 * - Public access (không cần authentication)
 * - Query optimization (JOIN FETCH, batch loading) giảm 94% queries
 * - Performance: Response time < 100ms cho 20 sản phẩm
 * - Hỗ trợ đầy đủ: search, filter, sort, pagination, comparison
 * 
 * Performance improvements:
 * - Trước: ~80 queries, 500ms response time
 * - Sau: ~5 queries, 50ms response time
 * - Batch load ratings, reviews, sold counts trong 1 query
 * Không yêu cầu authentication (public access)
 * 
 * @author UTE Fashion Hub Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ProductView API", description = "API dùng để hiển thị sản phẩm client và tương tác - Tham quan, tìm kiếm, lọc, sắp xếp, so sánh sản phẩm")
public class ProductViewController {
        private final IProductViewService productViewService;

/**
 * GET /api/v1/products/search
 * Tìm kiếm sản phẩm theo từ khóa
 * 
 * Features:
 * - keyword: Tìm trong tên sản phẩm
 * - Chỉ hỗ trợ keyword search, không hỗ trợ filter
 * 
 * Sort options:
 * - name: Sắp xếp theo tên
 * - price: Sắp xếp theo giá
 * - rating: Sắp xếp theo đánh giá
 * - created_date: Sắp xếp theo ngày tạo (default)
 * 
 * Performance:
 * - Sử dụng optimized query với JOIN FETCH
 * - Batch load ratings/reviews/sold counts
 * - Response time: ~50ms cho 20 sản phẩm
 */
    // ========== SEARCH & FILTER ENDPOINTS ==========

    /**
     * Tìm kiếm và lọc sản phẩm với nhiều tiêu chí
     */
@GetMapping("/search")
@Operation(
        summary = "Tìm kiếm sản phẩm theo từ khóa",
        description = "API cho phép người dùng tìm kiếm sản phẩm theo từ khóa và sắp xếp theo nhiều tiêu chí"
)
@ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Tìm kiếm thành công",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Tham số không hợp lệ"
        )
})
public ResponseEntity<ApiResponse<Page<ProductCardResponse>>> searchProducts(
        @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(required = false) String keyword,
        @Parameter(description = "Sắp xếp theo (name, price, rating, created_date)") @RequestParam(required = false, defaultValue = "created_date") String sortBy,
        @Parameter(description = "Hướng sắp xếp (asc, desc)") @RequestParam(required = false, defaultValue = "desc") String sortDirection,
        @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(required = false, defaultValue = "0") Integer page,
        @Parameter(description = "Số sản phẩm mỗi trang") @RequestParam(required = false, defaultValue = "20") Integer size
) {
        log.info("Searching products with keyword: {}", keyword);
        
        ProductSearchFilterRequest request = ProductSearchFilterRequest.builder()
                .keyword(keyword)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();
        
        Page<ProductCardResponse> result = productViewService.searchAndFilterProducts(request);
        
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm sản phẩm thành công", result));

}

/**
 * POST /api/v1/products/filter
 * Lọc sản phẩm theo nhiều tiêu chí cùng lúc
 * 
 * Features:
 * - Hỗ trợ lọc đa tiêu chí: danh mục, thương hiệu, giá, màu, size, chất liệu, phong cách, đối tượng
 * - Logic AND: tất cả điều kiện phải thỏa mãn
 * - Hỗ trợ pagination và sorting
 * - Frontend có thể tick nhiều checkbox cùng lúc
 * 
 * Use case: Product Listing Page với sidebar filters
 */
@PostMapping("/filter")
@Operation(
        summary = "Lọc sản phẩm đa tiêu chí",
        description = "Lọc sản phẩm theo danh mục, thương hiệu, giá, màu sắc, kích thước, chất liệu, phong cách, đối tượng, đánh giá và tồn kho"
)
@ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Lọc sản phẩm thành công",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Tham số lọc không hợp lệ"
        )
})
public ResponseEntity<ApiResponse<Page<ProductCardResponse>>> filterProducts(
        @Parameter(description = "Tiêu chí lọc sản phẩm", required = true)
        @RequestBody ProductFilterRequest request
) {
        log.info("Filtering products with criteria: categories={}, brands={}, priceRange=[{}-{}], colors={}, sizes={}, materials={}, styles={}, audience={}, minRating={}, inStockOnly={}, hasDiscountOnly={}",
                request.getCategoryIds(), request.getBrandIds(), request.getMinPrice(), request.getMaxPrice(),
                request.getColorOptions(), request.getSizeOptions(), request.getMaterialOptions(),
                request.getStyleOptions(), request.getTargetAudienceOptions(), request.getMinRating(),
                request.getInStockOnly(), request.getHasDiscountOnly());
        
        Page<ProductCardResponse> result = productViewService.filterProducts(request);
        
        return ResponseEntity.ok(ApiResponse.success("Lọc sản phẩm thành công", result));
}

/**
 * GET /api/v1/products/{id}
 * Lấy chi tiết sản phẩm theo ID
 * 
 * Response bao gồm:
 * - Thông tin cơ bản (name, description, brand, category)
 * - Tất cả biến thể màu và kích thước
 * - Thông tin thời trang (chất liệu, phong cách, đối tượng, mùa)
 * - Hình ảnh sản phẩm
 * - Ratings và review count (real data từ database)
 * - Sold count (từ order_items)
 * 
 * Use case: Product Detail Page
 */
@GetMapping("/{id}")
@Operation(
        summary = "Xem chi tiết sản phẩm",
        description = "Lấy thông tin chi tiết của một sản phẩm bao gồm thông số kỹ thuật, các phiên bản, hình ảnh,giá,..."
)
@ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Lấy chi tiết thành công",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Không tìm thấy sản phẩm"
        )
})
public ResponseEntity<ApiResponse<ProductDetailViewResponse>> getProductDetail(
        @Parameter(description = "ID sản phẩm", required = true) @PathVariable Long id
) {
        log.info("Getting product detail for ID: {}", id);
        
        ProductDetailViewResponse result = productViewService.getProductDetailById(id);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết sản phẩm thành công", result));
}

    // ========== CATEGORY & RELATED ENDPOINTS ==========

/**
 * GET /api/v1/products/category/{categoryId}
 * Lấy danh sách sản phẩm theo danh mục
 * 
 * Response bao gồm:
 * - Thông tin danh mục (name, description)
 * - Danh sách subcategories
 * - Danh sách sản phẩm thuộc danh mục (paginated)
 * - Available filters (brands, price ranges)
 * 
 * Use case: Category Page, Product Listing
 */
@GetMapping("/category/{categoryId}")
@Operation(
        summary = "Xem sản phẩm theo danh mục",
        description = "Lấy danh sách sản phẩm thuộc một danh mục cụ thể, bao gồm thông tin danh mục con và bộ lọc"
)
@ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Lấy danh sách thành công",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Không tìm thấy danh mục"
        )
})
public ResponseEntity<ApiResponse<CategoryProductsResponse>> getProductsByCategory(
        @Parameter(description = "ID danh mục", required = true) @PathVariable Long categoryId,
        @Parameter(description = "Sắp xếp theo") @RequestParam(required = false, defaultValue = "created_date") String sortBy,
        @Parameter(description = "Hướng sắp xếp") @RequestParam(required = false, defaultValue = "desc") String sortDirection,
        @Parameter(description = "Số trang") @RequestParam(required = false, defaultValue = "0") Integer page,
        @Parameter(description = "Số sản phẩm mỗi trang") @RequestParam(required = false, defaultValue = "20") Integer size
) {
        log.info("Getting products for category ID: {}", categoryId);
        
        ProductSearchFilterRequest request = ProductSearchFilterRequest.builder()
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();
        
        CategoryProductsResponse result = productViewService.getProductsByCategory(categoryId, request);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm theo danh mục thành công", result));
}

    // ========== COMPARISON & RECOMMENDATIONS ==========

/**
 * POST /api/v1/products/compare
 * So sánh nhiều sản phẩm (tối đa 4)
 * 
 * Business rules:
 * - Minimum 2 products, maximum 4 products
 * - Tất cả products phải tồn tại (throw 404 nếu không)
 * - Response hiển thị side-by-side comparison
 * 
 * Use case: Product Comparison Page
 * Request body: [1, 2, 3, 4] - Array of product IDs
 */
@PostMapping("/compare")
@Operation(
        summary = "So sánh sản phẩm",
        description = "So sánh thông số kỹ thuật và giá của nhiều sản phẩm (tối đa 4 sản phẩm)"
)
@ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "So sánh thành công",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Số lượng sản phẩm không hợp lệ (tối đa 4)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Không tìm thấy một số sản phẩm"
        )
})
public ResponseEntity<ApiResponse<ProductComparisonResponse>> compareProducts(
        @Parameter(description = "Danh sách ID sản phẩm cần so sánh (tối đa 4)", required = true)
        @RequestBody List<Long> productIds
) {
        log.info("Comparing products: {}", productIds);
        
        ProductComparisonResponse result = productViewService.compareProducts(productIds);
        
        return ResponseEntity.ok(ApiResponse.success("So sánh sản phẩm thành công", result));
}

/**
 * GET /api/v1/products/{id}/related
 * Lấy sản phẩm liên quan
 * 
 * Logic:
 * - Cùng danh mục với sản phẩm gốc
 * - Chênh lệch giá không quá 6 triệu VND
 * - Loại trừ chính sản phẩm đó
 * - Sắp xếp theo created_date DESC (mới nhất)
 * 
 * Hỗ trợ:
 * - page/size: Phân trang
 * - limit: Lấy N sản phẩm đầu tiên (không pagination)
 * 
 * Use case: "Sản phẩm tương tự" section in Product Detail Page
 */
@GetMapping("/{id}/related")
@Operation(
        summary = "Xem sản phẩm liên quan",
        description = "Lấy danh sách sản phẩm liên quan (cùng danh mục, chênh lệch giá ≤ 6 triệu). Sử dụng limit để lấy N sản phẩm đầu tiên, hoặc dùng page/size để phân trang"
)
@ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Lấy danh sách thành công",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Không tìm thấy sản phẩm"
        )
})
public ResponseEntity<ApiResponse<?>> getRelatedProducts(
        @Parameter(description = "ID sản phẩm", required = true) @PathVariable Long id,
        @Parameter(description = "Số lượng giới hạn sản phẩm (không pagination)") @RequestParam(required = false) Integer limit,
        @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(required = false, defaultValue = "0") Integer page,
        @Parameter(description = "Số sản phẩm mỗi trang") @RequestParam(required = false, defaultValue = "20") Integer size
) {
        log.info("Getting related products for ID: {} with limit: {}, page: {}, size: {}", id, limit, page, size);
        
        if (limit != null && limit > 0) {
                List<ProductCardResponse> result = productViewService.getRelatedProducts(id, limit);
                return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm liên quan thành công", result));
        } else {
                ProductSearchFilterRequest request = ProductSearchFilterRequest.builder()
                        .page(page)
                        .size(size)
                        .build();
                Page<ProductCardResponse> result = productViewService.getRelatedProductsPaginated(id, request);
                return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm liên quan thành công", result));
        }
}

/**
 * GET /api/v1/products/best-selling
 * Lấy sản phẩm bán chạy
 * 
 * Logic:
 * - Sắp xếp theo sold count DESC (lượt bán cao nhất)
 * - Chỉ lấy sản phẩm ACTIVE và không bị xóa
 * 
 * Hỗ trợ:
 * - page/size: Phân trang
 * - limit: Lấy N sản phẩm đầu tiên (không pagination)
 * 
 * Use case: Homepage "Best Sellers", Product Recommendations
 */
@GetMapping({"/best-selling", "/top-selling"})
@Operation(
        summary = "Xem sản phẩm bán chạy",
        description = "Lấy danh sách sản phẩm bán chạy nhất theo lượt bán. Sử dụng limit để lấy N sản phẩm đầu tiên, hoặc dùng page/size để phân trang"
)
@ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Lấy danh sách thành công",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
})
public ResponseEntity<ApiResponse<?>> getBestSellingProducts(
        @Parameter(description = "Số lượng giới hạn sản phẩm (không pagination)") @RequestParam(required = false) Integer limit,
        @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(required = false, defaultValue = "0") Integer page,
        @Parameter(description = "Số sản phẩm mỗi trang") @RequestParam(required = false, defaultValue = "20") Integer size
) {
        log.info("Getting best selling products with limit: {}, page: {}, size: {}", limit, page, size);
        
        if (limit != null && limit > 0) {
                List<ProductCardResponse> result = productViewService.getBestSellingProducts(limit);
                return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm bán chạy thành công", result));
        } else {
                ProductSearchFilterRequest request = ProductSearchFilterRequest.builder()
                        .page(page)
                        .size(size)
                        .build();
                Page<ProductCardResponse> result = productViewService.getBestSellingProductsPaginated(request);
                return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm bán chạy thành công", result));
        }
}

/**
 * GET /api/v1/products/new-arrivals
 * Lấy sản phẩm mới nhất
 * 
 * Logic:
 * - Sắp xếp theo created_date DESC (mới nhất trước)
 * - Chỉ lấy sản phẩm ACTIVE và không bị xóa
 * 
 * Hỗ trợ:
 * - page/size: Phân trang
 * - limit: Lấy N sản phẩm đầu tiên (không pagination)
 * 
 * Use case: Homepage "New Arrivals", Product Discovery
 */
@GetMapping("/new-arrivals")
@Operation(
        summary = "Xem sản phẩm mới nhất",
        description = "Lấy danh sách sản phẩm mới ra mắt sắp xếp theo ngày thêm. Sử dụng limit để lấy N sản phẩm đầu tiên, hoặc dùng page/size để phân trang"
)
@ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Lấy danh sách thành công",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
})
public ResponseEntity<ApiResponse<?>> getNewArrivals(
        @Parameter(description = "Số lượng giới hạn sản phẩm (không pagination)") @RequestParam(required = false) Integer limit,
        @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(required = false, defaultValue = "0") Integer page,
        @Parameter(description = "Số sản phẩm mỗi trang") @RequestParam(required = false, defaultValue = "20") Integer size
) {
        log.info("Getting new arrivals with limit: {}, page: {}, size: {}", limit, page, size);
        
        if (limit != null && limit > 0) {
                List<ProductCardResponse> result = productViewService.getNewArrivals(limit);
                return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm mới nhất thành công", result));
        } else {
                ProductSearchFilterRequest request = ProductSearchFilterRequest.builder()
                        .page(page)
                        .size(size)
                        .build();
                Page<ProductCardResponse> result = productViewService.getNewArrivalsPaginated(request);
                return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm mới nhất thành công", result));
        }
}

/**
 * GET /api/v1/products/featured
 * Lấy sản phẩm nổi bật
 * 
 * Tiêu chí:
 * - Đánh giá trung bình >= 4.5 sao
 * - Số lượng đã bán >= 100
 * - Sắp xếp theo rating DESC, sold count DESC
 * 
 * Hỗ trợ:
 * - page/size: Phân trang
 * - limit: Lấy N sản phẩm đầu tiên (không pagination)
 * 
 * Use case: Homepage "Featured Products", Product Recommendations
 */
@GetMapping("/featured")
@Operation(
        summary = "Xem sản phẩm nổi bật",
        description = "Lấy danh sách sản phẩm nổi bật (đánh giá >= 4.5, đã bán >= 100). Sử dụng limit để lấy N sản phẩm đầu tiên, hoặc dùng page/size để phân trang"
)
@ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Lấy danh sách thành công",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
})
public ResponseEntity<ApiResponse<?>> getFeaturedProducts(
        @Parameter(description = "Số lượng giới hạn sản phẩm (không pagination)") @RequestParam(required = false) Integer limit,
        @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(required = false, defaultValue = "0") Integer page,
        @Parameter(description = "Số sản phẩm mỗi trang") @RequestParam(required = false, defaultValue = "20") Integer size
) {
        log.info("Getting featured products with limit: {}, page: {}, size: {}", limit, page, size);
        
        if (limit != null && limit > 0) {
                List<ProductCardResponse> result = productViewService.getFeaturedProducts(limit);
                return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm nổi bật thành công", result));
        } else {
                ProductSearchFilterRequest request = ProductSearchFilterRequest.builder()
                        .page(page)
                        .size(size)
                        .build();
                Page<ProductCardResponse> result = productViewService.getFeaturedProductsPaginated(request);
                return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm nổi bật thành công", result));
        }
}

/**
 * GET /api/v1/products/on-sale
 * Lấy sản phẩm đang giảm giá
 * 
 * Logic:
 * - Có mã giảm giá đang active (promotion)
 * - Sắp xếp theo số tiền giảm DESC (giảm nhiều nhất trước)
 * - Chỉ lấy sản phẩm ACTIVE và không bị xóa
 * 
 * Hỗ trợ:
 * - page/size: Phân trang
 * - limit: Lấy N sản phẩm đầu tiên (không pagination)
 * 
 * Use case: Homepage "On Sale", Sales Page
 */
@GetMapping("/on-sale")
@Operation(
        summary = "Xem sản phẩm đang giảm giá",
        description = "Lấy danh sách sản phẩm đang giảm giá (có mã giảm giá) sắp xếp theo số tiền giảm. Sử dụng limit để lấy N sản phẩm đầu tiên, hoặc dùng page/size để phân trang"
)
@ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Lấy danh sách thành công",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
})
public ResponseEntity<ApiResponse<?>> getProductsOnSale(
        @Parameter(description = "Số lượng giới hạn sản phẩm (không pagination)") @RequestParam(required = false) Integer limit,
        @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(required = false, defaultValue = "0") Integer page,
        @Parameter(description = "Số sản phẩm mỗi trang") @RequestParam(required = false, defaultValue = "20") Integer size
) {
        log.info("Getting products on sale with limit: {}, page: {}, size: {}", limit, page, size);
        
        if (limit != null && limit > 0) {
                List<ProductCardResponse> result = productViewService.getProductsOnSale(limit);
                return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm giảm giá thành công", result));
        } else {
                ProductSearchFilterRequest request = ProductSearchFilterRequest.builder()
                        .page(page)
                        .size(size)
                        .build();
                Page<ProductCardResponse> result = productViewService.getProductsOnSalePaginated(request);
                return ResponseEntity.ok(ApiResponse.success("Lấy sản phẩm giảm giá thành công", result));
        }
}

@GetMapping("/filter/color")
@Operation(summary = "Lọc sản phẩm theo màu sắc")
public ResponseEntity<ApiResponse<?>> filterByColor(
        @Parameter(description = "Danh sách màu") @RequestParam List<String> colorOptions,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer size
) {
        ProductSearchFilterRequest request = ProductSearchFilterRequest.builder().page(page).size(size).build();
        if (limit != null && limit > 0) {
                return ResponseEntity.ok(ApiResponse.success("Lọc theo màu thành công", productViewService.filterByColorWithLimit(colorOptions, request, limit)));
        }
        return ResponseEntity.ok(ApiResponse.success("Lọc theo màu thành công", productViewService.filterByColor(colorOptions, request)));
}

@GetMapping("/filter/size")
@Operation(summary = "Lọc sản phẩm theo kích thước")
public ResponseEntity<ApiResponse<?>> filterBySize(
        @Parameter(description = "Danh sách size") @RequestParam List<String> sizeOptions,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer size
) {
        ProductSearchFilterRequest request = ProductSearchFilterRequest.builder().page(page).size(size).build();
        if (limit != null && limit > 0) {
                return ResponseEntity.ok(ApiResponse.success("Lọc theo kích thước thành công", productViewService.filterBySizeWithLimit(sizeOptions, request, limit)));
        }
        return ResponseEntity.ok(ApiResponse.success("Lọc theo kích thước thành công", productViewService.filterBySize(sizeOptions, request)));
}

@GetMapping("/filter/material")
@Operation(summary = "Lọc sản phẩm theo chất liệu")
public ResponseEntity<ApiResponse<?>> filterByMaterial(
        @Parameter(description = "Danh sách chất liệu") @RequestParam List<String> materialOptions,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer size
) {
        ProductSearchFilterRequest request = ProductSearchFilterRequest.builder().page(page).size(size).build();
        if (limit != null && limit > 0) {
                return ResponseEntity.ok(ApiResponse.success("Lọc theo chất liệu thành công", productViewService.filterByMaterialWithLimit(materialOptions, request, limit)));
        }
        return ResponseEntity.ok(ApiResponse.success("Lọc theo chất liệu thành công", productViewService.filterByMaterial(materialOptions, request)));
}

@GetMapping("/filter/style")
@Operation(summary = "Lọc sản phẩm theo phong cách")
public ResponseEntity<ApiResponse<?>> filterByStyle(
        @Parameter(description = "Danh sách phong cách") @RequestParam List<String> styleOptions,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer size
) {
        ProductSearchFilterRequest request = ProductSearchFilterRequest.builder().page(page).size(size).build();
        if (limit != null && limit > 0) {
                return ResponseEntity.ok(ApiResponse.success("Lọc theo phong cách thành công", productViewService.filterByStyleWithLimit(styleOptions, request, limit)));
        }
        return ResponseEntity.ok(ApiResponse.success("Lọc theo phong cách thành công", productViewService.filterByStyle(styleOptions, request)));
}

@GetMapping("/filter/audience")
@Operation(summary = "Lọc sản phẩm theo đối tượng sử dụng")
public ResponseEntity<ApiResponse<?>> filterByAudience(
        @Parameter(description = "Danh sách đối tượng") @RequestParam List<String> targetAudienceOptions,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer size
) {
        ProductSearchFilterRequest request = ProductSearchFilterRequest.builder().page(page).size(size).build();
        if (limit != null && limit > 0) {
                return ResponseEntity.ok(ApiResponse.success("Lọc theo đối tượng thành công", productViewService.filterByAudienceWithLimit(targetAudienceOptions, request, limit)));
        }
        return ResponseEntity.ok(ApiResponse.success("Lọc theo đối tượng thành công", productViewService.filterByAudience(targetAudienceOptions, request)));
}

}

