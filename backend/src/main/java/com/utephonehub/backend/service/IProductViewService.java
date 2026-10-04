package com.utephonehub.backend.service;

import com.utephonehub.backend.dto.request.productview.ProductFilterRequest;
import com.utephonehub.backend.dto.request.productview.ProductSearchFilterRequest;
import com.utephonehub.backend.dto.response.productview.CategoryProductsResponse;
import com.utephonehub.backend.dto.response.productview.ProductCardResponse;
import com.utephonehub.backend.dto.response.productview.ProductComparisonResponse;
import com.utephonehub.backend.dto.response.productview.ProductDetailViewResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface cho ProductView API
 * API dành cho client-side: tìm kiếm, lọc, sắp xếp, so sánh sản phẩm
 */
public interface IProductViewService {
    
    /**
     * Tìm kiếm sản phẩm theo từ khóa
     * @param request Request chứa từ khóa tìm kiếm
     * @return Page chứa danh sách sản phẩm
     */
    Page<ProductCardResponse> searchAndFilterProducts(ProductSearchFilterRequest request);
    
    /**
     * Lọc sản phẩm theo đa tiêu chí
     * @param request Request chứa các tiêu chí lọc
     * @return Page chứa danh sách card sản phẩm
     */
    Page<ProductCardResponse> filterProducts(ProductFilterRequest request);
    
    /**
     * Lấy chi tiết sản phẩm theo ID
     * @param productId ID sản phẩm
     * @return Chi tiết sản phẩm cho client
     */
    ProductDetailViewResponse getProductDetailById(Long productId);
    
    /**
     * Lấy danh sách sản phẩm theo danh mục
     * @param categoryId ID danh mục
     * @param request Request chứa các tiêu chí lọc/sắp xếp
     * @return Thông tin danh mục và danh sách sản phẩm
     */
    CategoryProductsResponse getProductsByCategory(Long categoryId, ProductSearchFilterRequest request);
    
    /**
     * So sánh nhiều sản phẩm (tối đa 4 sản phẩm)
     * @param productIds Danh sách ID sản phẩm cần so sánh
     * @return Thông tin so sánh các sản phẩm
     */
    ProductComparisonResponse compareProducts(List<Long> productIds);
    
    /**
     * Lấy sản phẩm liên quan (cùng danh mục hoặc thương hiệu)
     * @param productId ID sản phẩm gốc
     * @param limit Số lượng sản phẩm liên quan
     * @return Danh sách sản phẩm liên quan
     */
    List<ProductCardResponse> getRelatedProducts(Long productId, Integer limit);
    
    /**
     * Lấy sản phẩm bán chạy
     * @param limit Số lượng sản phẩm
     * @return Danh sách sản phẩm bán chạy
     */
    List<ProductCardResponse> getBestSellingProducts(Integer limit);
    
    /**
     * Lấy sản phẩm mới nhất
     * @param limit Số lượng sản phẩm
     * @return Danh sách sản phẩm mới nhất
     */
    List<ProductCardResponse> getNewArrivals(Integer limit);
    
    /**
     * Lấy sản phẩm đang khuyến mãi
     * @param limit Số lượng sản phẩm
     * @return Danh sách sản phẩm khuyến mãi
     */
    List<ProductCardResponse> getFeaturedProducts(Integer limit);
    
    Page<ProductCardResponse> filterByColor(List<String> colorOptions, ProductSearchFilterRequest request);

    Page<ProductCardResponse> filterBySize(List<String> sizeOptions, ProductSearchFilterRequest request);

    Page<ProductCardResponse> filterByMaterial(List<String> materialOptions, ProductSearchFilterRequest request);

    Page<ProductCardResponse> filterByStyle(List<String> styleOptions, ProductSearchFilterRequest request);

    Page<ProductCardResponse> filterByAudience(List<String> targetAudienceOptions, ProductSearchFilterRequest request);
    
    /**
    // ===== WithLimit Methods =====
    
    /**
     * Tìm kiếm và lọc sản phẩm với giới hạn số lượng
     */
    List<ProductCardResponse> searchAndFilterProductsWithLimit(ProductSearchFilterRequest request, Integer limit);
    
    /**
     * Lấy sản phẩm theo danh mục với giới hạn số lượng
     */
    List<ProductCardResponse> getProductsByCategoryWithLimit(Long categoryId, ProductSearchFilterRequest request, Integer limit);
    
    List<ProductCardResponse> filterByColorWithLimit(List<String> colorOptions, ProductSearchFilterRequest request, Integer limit);

    List<ProductCardResponse> filterBySizeWithLimit(List<String> sizeOptions, ProductSearchFilterRequest request, Integer limit);

    List<ProductCardResponse> filterByMaterialWithLimit(List<String> materialOptions, ProductSearchFilterRequest request, Integer limit);

    List<ProductCardResponse> filterByStyleWithLimit(List<String> styleOptions, ProductSearchFilterRequest request, Integer limit);

    List<ProductCardResponse> filterByAudienceWithLimit(List<String> targetAudienceOptions, ProductSearchFilterRequest request, Integer limit);
    
    // ===== NEW ENHANCED METHODS =====
    
    /**
     * Lấy danh sách sản phẩm nổi bật theo nhiều tiêu chí
     * Tiêu chí: Giá >= 5 triệu, Hàng mới (60 ngày), Đánh giá >= 4.8, Số đánh giá >= 10, Có giảm giá
     * @param limit Số lượng sản phẩm
     * @return Danh sách sản phẩm nổi bật
     */
    List<ProductCardResponse> getFeaturedProductsByCriteria(Integer limit);
    
    /**
     * Lọc sản phẩm theo số lượng đã bán
     * @param minSoldCount Số lượng bán tối thiểu
     * @param request Request chứa các tiêu chí lọc khác
     * @return Page chứa danh sách sản phẩm
     */
    Page<ProductCardResponse> filterBySoldCount(Integer minSoldCount, ProductSearchFilterRequest request);
    
    /**
     * Lấy tất cả sản phẩm (bao gồm cả hết hàng và còn hàng)
     * @param request Request chứa các tiêu chí lọc/sắp xếp
     * @return Page chứa danh sách tất cả sản phẩm
     */
    Page<ProductCardResponse> getAllProducts(ProductSearchFilterRequest request);
    
    /**
     * Lấy chi tiết sản phẩm kèm số lượng đã bán từ order_items
     * @param productId ID sản phẩm
     * @return Chi tiết sản phẩm với thông tin số lượng đã bán
     */
    ProductDetailViewResponse getProductDetailWithSoldCount(Long productId);
    
    // ===== NEW AI-ENHANCED METHODS =====
    
    /**
     * [Client] Lấy danh sách sản phẩm nổi bật với pagination
     */
    Page<ProductCardResponse> getFeaturedProductsPaginated(ProductSearchFilterRequest request);
    
    /**
     * [Client] Lấy danh sách sản phẩm mới nhất với pagination
     */
    Page<ProductCardResponse> getNewArrivalsPaginated(ProductSearchFilterRequest request);
    
    /**
     * [Client] Lấy danh sách sản phẩm bán chạy với pagination
     */
    Page<ProductCardResponse> getBestSellingProductsPaginated(ProductSearchFilterRequest request);
    
    /**
     * [Client] Lấy danh sách sản phẩm đang giảm giá với pagination
     */
    Page<ProductCardResponse> getProductsOnSalePaginated(ProductSearchFilterRequest request);
    
    /**
     * [Client] Lấy sản phẩm liên quan với pagination
     */
    Page<ProductCardResponse> getRelatedProductsPaginated(Long productId, ProductSearchFilterRequest request);
    
    /**
     * [Client] Lấy sản phẩm đang giảm giá (List mode)
     */
    List<ProductCardResponse> getProductsOnSale(Integer limit);
}
