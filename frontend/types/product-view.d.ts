/**
 * Product View Types - For public product viewing APIs
 * Based on ProductViewController endpoints
 */

// ==================== PRODUCT VIEW RESPONSE ====================

export interface ProductImageInfo {
  id: number;
  imageUrl: string;
  altText?: string;
  isPrimary: boolean;
  imageOrder: number;
}

export interface ProductViewResponse {
  id: number;
  name: string;
  description?: string;
  thumbnailUrl?: string;
  
  // Category & Brand
  categoryId: number;
  categoryName: string;
  brandId: number;
  brandName: string;
  
  // Pricing
  minPrice: number;
  maxPrice: number;
  
  // Rating & Reviews
  averageRating: number;
  totalReviews: number;
  
  // Stock
  inStock: boolean;
  totalStock: number;
  soldCount: number;
  
  // Images
  images: ProductImageInfo[];
  variantsCount: number;
  
  color?: string;
  size?: string;
  material?: string;
  style?: string;
  targetAudience?: string;

  templates?: ProductTemplateInfo[];
  
  // Promotion
  promotionBadge?: string;
  discountPercentage?: number;
}

// ==================== PRODUCT DETAIL ====================

export interface ProductTemplateInfo {
  id: number;
  sku: string;
  color?: string;
  size?: string;
  price: number;
  stockQuantity: number;
  status: boolean;
}

// New API Response Types
export interface CategoryInfo {
  id: number;
  name: string;
  slug: string;
}

export interface BrandInfo {
  id: number;
  name: string;
  logoUrl: string;
}

export interface ProductVariant {
  id: number;
  sku: string;
  color?: string;
  size?: string;
  originalPrice?: number;
  discountedPrice?: number;
  discountAmount?: number;
  discountPercentage?: number;
  stockQuantity?: number;
  stockStatus?: string;
  status?: boolean;
}

export interface TechnicalSpecs {
  material?: string;
  style?: string;
  targetAudience?: string;
  season?: string;
  pattern?: string;
  fit?: string;
  origin?: string;
  careInstructions?: string;
  additionalSpecs?: string;
}

export interface ProductDetailViewResponse extends ProductViewResponse {
  id: number;
  name: string;
  description?: string;
  thumbnailUrl?: string;

  category: CategoryInfo;
  brand: BrandInfo;

  images: ProductImageInfo[];
  variants: ProductVariant[];
  fashionInfo?: TechnicalSpecs;
  technicalSpecs?: TechnicalSpecs;

  averageRating: number;
  totalReviews: number;
  inStock: boolean;
}

export type ProductDetail = ProductDetailViewResponse;

// ==================== SEARCH & FILTER REQUEST ====================

export interface ProductSearchFilterRequest {
  // Search only - for GET /api/v1/products/search
  keyword?: string;
  
  // Sort
  sortBy?: 'name' | 'price' | 'rating' | 'created_date';
  sortDirection?: 'asc' | 'desc';
  
  // Pagination
  page?: number;
  size?: number;
}

// Separate interface for advanced filtering - for POST /api/v1/products/filter  
export interface ProductFilterRequest {
  // Category & Brand
  categoryIds?: number[];
  brandIds?: number[];
  
  // Price range
  minPrice?: number;
  maxPrice?: number;
  
  colorOptions?: string[];
  sizeOptions?: string[];
  materialOptions?: string[];
  styleOptions?: string[];
  targetAudienceOptions?: string[];
  
  // Rating & Status
  minRating?: number;
  inStockOnly?: boolean;
  hasDiscountOnly?: boolean;
  
  // Sort & Pagination
  sortBy?: 'name' | 'price' | 'rating' | 'created_date';
  sortDirection?: 'asc' | 'desc';
  page?: number;
  size?: number;
}

// ==================== CATEGORY PRODUCTS ====================

export interface CategoryInfo {
  id: number;
  name: string;
  description?: string;
  imageUrl?: string;
}

export interface SubcategoryInfo {
  id: number;
  name: string;
  productCount: number;
}

export interface BrandFilterInfo {
  id: number;
  name: string;
  productCount: number;
}

export interface PriceRangeInfo {
  min: number;
  max: number;
  label: string;
  productCount: number;
}

export interface CategoryProductsResponse {
  category: CategoryInfo;
  subcategories: SubcategoryInfo[];
  products: {
    content: ProductViewResponse[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
  };
  availableFilters: {
    brands: BrandFilterInfo[];
    priceRanges: PriceRangeInfo[];
  };
}

// ==================== PRODUCT COMPARISON ====================

export interface ProductComparisonItem {
  id: number;
  name: string;
  brandName: string;
  thumbnailUrl?: string;
  price: number;
  rating: number;
  specifications: Record<string, any>;
}

export interface ProductComparisonResponse {
  products: ComparisonProduct[];
}

export interface ComparisonProduct {
  id: number;
  name: string;
  thumbnailUrl?: string;
  brandName: string;
  
  // Price
  originalPrice: number;
  discountedPrice?: number;
  hasDiscount: boolean;
  
  // Rating
  averageRating: number;
  totalReviews: number;
  
  // Stock
  inStock: boolean;
  
  // Technical Specs for Comparison
  specs: ComparisonSpecs;
}

export interface ComparisonSpecs {
  material?: string;
  style?: string;
  targetAudience?: string;
  fit?: string;
  season?: string;
  pattern?: string;
  origin?: string;
  colors?: string;
  sizes?: string;
  careInstructions?: string;
}

// ==================== PAGINATION RESPONSE ====================

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

// ==================== FILTER OPTIONS FOR UI ====================

export interface FilterOption {
  label: string;
  value: string | number;
  count?: number;
}

export interface PriceRangeFilter {
  min: number;
  max: number;
  label: string;
}

export interface RatingFilter {
  value: number;
  label: string;
}

export interface SortOption {
  label: string;
  value: string;
  sortBy: string;
  sortDirection: 'asc' | 'desc';
}

// ==================== UI STATE ====================

export interface ProductListingState {
  products: ProductViewResponse[];
  loading: boolean;
  error: string | null;
  totalPages: number;
  totalElements: number;
  currentPage: number;
  filters: ProductSearchFilterRequest;
}
